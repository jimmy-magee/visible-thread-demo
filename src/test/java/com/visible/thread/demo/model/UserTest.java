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

public class UserTest {

    User user = User.builder().build();

    @Test
    public void beanIsSerializable() throws Exception {
        final User myBean = user;
        final byte[] serializedMyBean = SerializationUtils.serialize((Serializable) myBean);
        final User deserializedMyBean = (User) SerializationUtils.deserialize(serializedMyBean);
        assertEquals(myBean, deserializedMyBean);
    }

    @Test
    public void getterAndSetterCorrectness() throws Exception {
        BeanTester tester = new BeanTester();
        tester.getFactoryCollection().addFactory(LocalDateTime.class, new LocalDateTimeFactory());
        tester.testBean(User.class);
    }

    @Test
    public void equalsAndHashCodeContract() throws Exception {

        EqualsVerifier.forClass(User.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void testToString() {
        String text = User.builder().email("test").toString();
        assertThat(text, not(emptyString()));
    }

}
