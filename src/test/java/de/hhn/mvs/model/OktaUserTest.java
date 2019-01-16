package de.hhn.mvs.model;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Testing {@link OktaUser}
 */
public class OktaUserTest {

    private static final String A_GROUP_ID = "something";

    private User nonOktaUser;
    private List<String> groupIds = new ArrayList<>(Arrays.asList(A_GROUP_ID));
    private OktaUser oktaUser;

    @Before
    public void setUp() {
        nonOktaUser = new UserImpl("anId", false, "foo@hoo.ch", "foobar", "imunused", "aname", null);
        oktaUser = OktaUser.create(nonOktaUser, groupIds);
    }

    @Test
    public void testCreateOktaUser_WithUser_ExpectNotNull() {
        assertNotEquals(null, oktaUser);
        assertEquals(1, oktaUser.getGroupIds().size());
        assertEquals(A_GROUP_ID, oktaUser.getGroupIds().get(0));
    }

    @Test
    public void testToString() {
        assertEquals("OktaUser{profile={firstName=aname, login=foo@hoo.ch, email=foo@hoo.ch}, credentials=OktaCredentials{password={value=foobar}}, groupIds=[something]}", oktaUser.toString());
    }

    @Test
    public void testSetProfile_WithEmail() {
        String email = "something@foo.ch";
        Map<String, String> profile = new HashMap<>();
        profile.put("email", email);
        oktaUser.setProfile(profile);
        assertEquals(email, oktaUser.getProfile().get("email"));
    }

    @Test
    public void testEquals_WhenBothAreEquals_ExpectTrue() {
        OktaUser anotherUser = OktaUser.create(nonOktaUser, groupIds);
        assertEquals(anotherUser, oktaUser);
    }

    @Test
    public void testEquals_WhenNotEquals_ExpectFalse() {
        assertNotEquals(oktaUser, OktaUser.create(nonOktaUser, new ArrayList<>()));
    }

    @Test
    public void testEquals_WithNull_ExpectFalse() {
        assertFalse(oktaUser.equals(null));
    }

}
