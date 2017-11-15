package fr.quatrevieux.araknemu.realm;

import fr.quatrevieux.araknemu.data.living.entity.account.Account;
import fr.quatrevieux.araknemu.data.living.repository.account.AccountRepository;
import fr.quatrevieux.araknemu.network.realm.RealmSession;
import fr.quatrevieux.araknemu.network.realm.out.*;
import org.apache.mina.core.session.DummySession;
import org.apache.mina.core.session.IoSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationProtocolTest extends RealmBaseCase {
    private AccountRepository repository;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        repository = new AccountRepository(
            app.database().get("realm")
        );

        repository.initialize();

        repository.add(new Account(-1, "test", "password", "pseudo"));
    }

    @AfterEach
    void tearDown() throws SQLException {
        dropTable("ACCOUNT");
    }

    @Test
    void failWithBadVersion() throws Exception {
        sendPacket("1.0.4");

        requestStack.assertLast(new BadVersion("1.29.1"));
        assertClosed();
    }

    @Test
    void failWithBadCredentials() throws Exception {
        sendPacket("1.29.1");
        sendPacket("login\n#1password");

        requestStack.assertLast(new LoginError(LoginError.LOGIN_ERROR));
        assertClosed();
    }

    @Test
    void failBadPassword() throws Exception {
        sendPacket("1.29.1");
        sendPacket("test\n#1"+ConnectionKeyTest.cryptPassword("bad_password", session.key().key()));

        requestStack.assertLast(new LoginError(LoginError.LOGIN_ERROR));
        assertClosed();
    }

    @Test
    void authenticationSuccess() throws Exception {
        sendPacket("1.29.1");
        sendPacket("test\n#1"+ConnectionKeyTest.cryptPassword("password", session.key().key()));

        assertTrue(session.isLogged());
        assertEquals("pseudo", session.account().pseudo());

        requestStack.assertAll(
            new Pseudo("pseudo"),
            new Community(0),
            new GMLevel(false),
            new Answer("")
        );
    }

    @Test
    void authenticateTwiceError() throws Exception {
        IoSession io = new DummySession();
        RealmSession s1 = new RealmSession(io, true);

        ioHandler.messageReceived(io, "1.29.1");
        ioHandler.messageReceived(io,"test\n#1"+ConnectionKeyTest.cryptPassword("password", s1.key().key()));

        assertTrue(s1.isLogged());
        assertTrue(s1.account().isAlive());

        // Authenticate with second session on same account
        sendPacket("1.29.1");
        sendPacket("test\n#1"+ConnectionKeyTest.cryptPassword("password", session.key().key()));

        assertFalse(session.isLogged());
        requestStack.assertLast(new LoginError(LoginError.ALREADY_LOGGED));

        assertTrue(s1.isLogged());
        assertTrue(s1.account().isAlive());
    }

    @Test
    void authenticateAndLogout() throws Exception {
        IoSession io = new DummySession();
        RealmSession s1 = new RealmSession(io, true);

        ioHandler.messageReceived(io, "1.29.1");
        ioHandler.messageReceived(io,"test\n#1"+ConnectionKeyTest.cryptPassword("password", s1.key().key()));
        s1.close();

        assertFalse(s1.account().isAlive());

        // Authenticate with second session on same account
        sendPacket("1.29.1");
        sendPacket("test\n#1"+ConnectionKeyTest.cryptPassword("password", session.key().key()));

        assertTrue(session.isLogged());
    }
}
