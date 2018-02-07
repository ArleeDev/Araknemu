package fr.quatrevieux.araknemu.data.world.transformer;

import fr.quatrevieux.araknemu._test.TestCase;
import fr.quatrevieux.araknemu.data.constant.Effect;
import fr.quatrevieux.araknemu.data.value.ItemTemplateEffectEntry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemEffectsTransformerTest extends TestCase {
    private ItemEffectsTransformer transformer = new ItemEffectsTransformer();

    @Test
    void serializeNull() {
        assertEquals("", transformer.serialize(null));
    }

    @Test
    void serializeEmpty() {
        assertEquals("", transformer.serialize(new ArrayList<>()));
    }

    @Test
    void serializeWithOneEffect() {
        assertEquals(
            "63#a#f#0#1d6+9",
            transformer.serialize(Arrays.asList(new ItemTemplateEffectEntry(Effect.INFLICT_DAMAGE_FIRE, 10, 15, 0, "1d6+9")))
        );
    }

    @Test
    void serializeWithTwoEffects() {
        assertEquals(
            "63#a#f#0#1d6+9,7e#5#a#0#1d6+4",
            transformer.serialize(Arrays.asList(
                new ItemTemplateEffectEntry(Effect.INFLICT_DAMAGE_FIRE, 10, 15, 0, "1d6+9"),
                new ItemTemplateEffectEntry(Effect.ADD_INTELLIGENCE, 5, 10, 0, "1d6+4")
            ))
        );
    }

    @Test
    void unserializeComplex() {
        List<ItemTemplateEffectEntry> effects = transformer.unserialize("64#b#f#0#1d5+10,7d#b#0#0#0d0+11,9a#f#0#0#0d0+15");

        assertCount(3, effects);

        assertEquals(Effect.INFLICT_DAMAGE_NEUTRAL, effects.get(0).effect());
        assertEquals(11, effects.get(0).min());
        assertEquals(15, effects.get(0).max());
        assertEquals(0, effects.get(0).special());

        assertEquals(Effect.ADD_VITALITY, effects.get(1).effect());
        assertEquals(11, effects.get(1).min());
        assertEquals(0, effects.get(1).max());
        assertEquals(0, effects.get(1).special());

        assertEquals(Effect.SUB_AGILITY, effects.get(2).effect());
        assertEquals(15, effects.get(2).min());
        assertEquals(0, effects.get(2).max());
        assertEquals(0, effects.get(2).special());
    }

    @Test
    void unserializeWithoutText() {
        List<ItemTemplateEffectEntry> effects = transformer.unserialize("64#b#f#0");

        assertCount(1, effects);

        assertEquals(Effect.INFLICT_DAMAGE_NEUTRAL, effects.get(0).effect());
        assertEquals(11, effects.get(0).min());
        assertEquals(15, effects.get(0).max());
        assertEquals(0, effects.get(0).special());
    }

    @Test
    void unserializeNull() {
        assertTrue(transformer.unserialize(null).isEmpty());
    }

    @Test
    void unserializeEmpty() {
        assertTrue(transformer.unserialize("").isEmpty());
    }

    @Test
    void unserializeInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> transformer.unserialize("invalid data"));
    }
}
