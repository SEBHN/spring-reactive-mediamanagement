package de.hhn.mvs;


import de.hhn.mvs.rest.FolderHandlerTest;
import de.hhn.mvs.rest.MediaHandlerTest;
import de.hhn.mvs.rest.OktaHandlerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        FolderHandlerTest.class,
        MediaHandlerTest.class,
        OktaHandlerTest.class,
})
public class FeatureTests {
}
