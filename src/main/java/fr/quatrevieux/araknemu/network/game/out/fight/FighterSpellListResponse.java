package fr.quatrevieux.araknemu.network.game.out.fight;

import fr.arakne.utils.encoding.Base64;
import fr.quatrevieux.araknemu.game.fight.fighter.FighterSpellList;
import fr.quatrevieux.araknemu.game.spell.Spell;

/**
 * Constructs a string containing all spells in the provided list
 */
public final class FighterSpellListResponse {
    private final FighterSpellList spells;

    public FighterSpellListResponse(FighterSpellList spells) {
        this.spells = spells;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SL");
        int position = 1;

        for (Spell spell : spells.get()) {
            if(position>63)
                throw new IllegalArgumentException("spells on FighterSpellListResponse too big");

            sb.append(spell.id()).append("~")
                    .append(spell.level()).append("~")
                    .append(Base64.chr(position)).append(";");
            position++;
        }
        if (sb.length() > 1 && sb.charAt(sb.length() - 1) == ';') {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }
}
