package de.hhn.mvs.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

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
    private User aUserWithRoles;

    @Before
    public void setUp() {
        aUser = new UserImpl("anId", false, "foo@hoo.ch", "foobar", "imunused", "aname", null);
        aUserWithRoles = new UserImpl("anId", false, "foo@hoo.ch", "foobar", "imunused", "aname", userRoles);
    }

    @Test
    public void testGetAuthorities_WhenPassedNull_ExpectNoAuthorities() {
        assertTrue(aUser.getRoles().isEmpty());
        assertTrue(aUser.getAuthorities().isEmpty());
    }

    @Test
    public void testGetAuthorities_WhenPassingRole_ExpectRolesAndAuthorities() {
        assertEquals(1, aUserWithRoles.getAuthorities().size());
        assertEquals(1, aUserWithRoles.getRoles().size());
        assertEquals(USER_ROLE, aUserWithRoles.getRoles().get(0));
        assertEquals(USER_ROLE, aUserWithRoles.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    public void testToString(){
        assertEquals("UserImpl{id='anId', email='foo@hoo.ch', admin=false, token='imunused', name='aname', password='foobar', roles=[ROLE_USER]}", aUserWithRoles.toString());
    }

    @Test
    public void testEquals_WhenBothAreEquals_ExpectTrue(){
        User anotherUser = new UserImpl(aUserWithRoles.getId(), aUserWithRoles.isAdmin(), aUserWithRoles.getEmail(), aUserWithRoles.getPassword(), aUserWithRoles.getToken(), aUserWithRoles.getName(), aUserWithRoles.getRoles());
        assertEquals(anotherUser, aUserWithRoles);
    }

    @Test
    public void testEquals_WhenNotEquals_ExpectFalse(){
        assertNotEquals(aUser, aUserWithRoles);
    }

    @Test
    public void testEquals_WithNull_ExpectFalse(){
        assertFalse(aUser.equals(null));
    }

    @Test
    public void testHashCode_WhenBothAreEquals_ExpectSame(){
        User anotherUser = new UserImpl(aUserWithRoles.getId(), aUserWithRoles.isAdmin(), aUserWithRoles.getEmail(), aUserWithRoles.getPassword(), aUserWithRoles.getToken(), aUserWithRoles.getName(), aUserWithRoles.getRoles());
        assertEquals(anotherUser.hashCode(), aUserWithRoles.hashCode());
    }

    @Test
    public void testHashCode_WhenNotEquals_ExpectFalse(){
        assertNotEquals(aUser.hashCode(), aUserWithRoles.hashCode());
    }
}
