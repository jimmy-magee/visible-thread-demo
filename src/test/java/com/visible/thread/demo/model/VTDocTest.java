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

public class VTDocTest {

    VTDoc vTDoc = VTDoc.builder().build();

    @Test
    public void beanIsSerializable() throws Exception {
        final VTDoc myBean = vTDoc;
        final byte[] serializedMyBean = SerializationUtils.serialize((Serializable) myBean);
        final VTDoc deserializedMyBean = (VTDoc) SerializationUtils.deserialize(serializedMyBean);
        assertEquals(myBean, deserializedMyBean);
    }

    @Test
    public void getterAndSetterCorrectness() throws Exception {
        BeanTester tester = new BeanTester();
        tester.getFactoryCollection().addFactory(LocalDateTime.class, new LocalDateTimeFactory());
        tester.testBean(VTDoc.class);
    }

    @Test
    public void equalsAndHashCodeContract() throws Exception {

        EqualsVerifier.forClass(VTDoc.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    public void testToString() {
        String text = VTDoc.builder().name("test").toString();
        assertThat(text, not(emptyString()));
    }

}


