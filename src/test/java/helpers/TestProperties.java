package helpers;

import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"system:properties",
        "system:env",
        "file:target/test-classes/activeProfile.properties"})
public interface TestProperties extends Config {
    @Key("chrome.dir")
    String chromeDir();

    @Key("chrome.profile.dir")
    String profileDir();

    @Key("use.browser.profile")
    boolean useChromeProfile();

    @Key("maven.profile")
    String activeProfileId();

    @Key("browser.headless")
    boolean browserHeadless();

    @Key("use.selenoid")
    boolean useSelenoid();
}
