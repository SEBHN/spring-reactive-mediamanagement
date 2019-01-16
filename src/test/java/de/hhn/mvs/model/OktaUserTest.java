package de.hhn.mvs.model;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Testing {@link OktaUser}
 */
public class OktaUserTest {

    private UserImpl nonOktaUser;

    @Before
    public void setUp(){
        nonOktaUser = new UserImpl("anId", false, "foo@hoo.ch", "foobar", "imunused", "aname", null);
    }

    @Test
    public void testCreateOktaUser(){
        OktaUser oktaUser = OktaUser.create(nonOktaUser, new ArrayList<>(Arrays.asList("something")));
        assertNotEquals(null, oktaUser);
        assertEquals(1, oktaUser.getGroupIds().size());
        assertEquals("something", oktaUser.getGroupIds().get(0));
    }
}
