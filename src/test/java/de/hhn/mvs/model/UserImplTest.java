package de.hhn.mvs.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testing {@link UserImpl}
 */
public class UserImplTest {

    private static final String USER_ROLE = "ROLE_USER";

    private List<String> userRoles = new ArrayList<>(Arrays.asList(USER_ROLE));
    private User aUser;
    private User aUserWithAnotherName;

    @Before
    public void setUp() {
        aUser = new UserImpl("foo@hoo.ch", "foobar", "aname");
        aUserWithAnotherName = new UserImpl("foo@hoo.ch", "foobar", "anotherName");
    }

    @Test
    public void testToString(){
        assertEquals("UserImpl{, email='foo@hoo.ch', name='aname', password='foobar'}", aUser.toString());
    }

    @Test
    public void testEquals_WhenBothAreEquals_ExpectTrue(){
        User anotherUser = new UserImpl(aUserWithAnotherName.getEmail(), aUserWithAnotherName.getPassword(), aUserWithAnotherName.getName());
        assertEquals(anotherUser, aUserWithAnotherName);
    }

    @Test
    public void testEquals_WhenNotEquals_ExpectFalse(){
        assertNotEquals(aUser, aUserWithAnotherName);
    }

    @Test
    public void testEquals_WithNull_ExpectFalse(){
        assertFalse(aUser.equals(null));
    }

    @Test
    public void testEquals_WithOtherObject_ExpectFalse(){
        assertFalse(aUser.equals(""));
    }

    @Test
    public void testEquals_WithSameObject_ExpectTrue(){
        assertTrue(aUser.equals(aUser));
    }

    @Test
    public void testEquals_WithDifferentEmail_ExpectFalse(){
        aUserWithAnotherName.setEmail("anotherMail@foo.com");
        assertFalse(aUser.equals(aUserWithAnotherName));
    }

    @Test
    public void testEquals_WithDifferentPassword_ExpectFalse(){
        aUserWithAnotherName.setName(aUser.getName());
        aUserWithAnotherName.setPassword("something");
        assertFalse(aUser.equals(aUserWithAnotherName));
    }

    @Test
    public void testHashCode_WhenBothAreEquals_ExpectSame(){
        User anotherUser = new UserImpl(aUserWithAnotherName.getEmail(), aUserWithAnotherName.getPassword(), aUserWithAnotherName.getName());
        assertEquals(anotherUser.hashCode(), aUserWithAnotherName.hashCode());
    }

    @Test
    public void testHashCode_WhenNotEquals_ExpectFalse(){
        assertNotEquals(aUser.hashCode(), aUserWithAnotherName.hashCode());
    }
}
