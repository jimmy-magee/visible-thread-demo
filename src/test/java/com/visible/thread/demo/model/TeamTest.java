package com.visible.thread.demo.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import org.meanbean.test.BeanTester;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TeamTest {

    Team team = Team.builder().build();

    @Test
    public void beanIsSerializable() throws Exception {
        final Team myBean = team;
        final byte[] serializedMyBean = SerializationUtils.serialize((Serializable) myBean);
        final Team deserializedMyBean = (Team) SerializationUtils.deserialize(serializedMyBean);
        assertEquals(myBean, deserializedMyBean);
    }

    @Test
    public void getterAndSetterCorrectness() throws Exception {
        BeanTester tester = new BeanTester();
        tester.getFactoryCollection().addFactory(LocalDateTime.class, new LocalDateTimeFactory());
        tester.testBean(Team.class);
    }

    @Test
    public void equalsAndHashCodeContract() throws Exception {

        EqualsVerifier.forClass(Team.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void testToString() {
        String text = Team.builder().name("test").toString();
        assertThat(text, not(emptyString()));
    }

}

