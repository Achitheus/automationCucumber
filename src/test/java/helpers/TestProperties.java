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

    @Key("use.chrome.profile")
    boolean useChromeProfile();

    @Key("active.profile")
    String activeProfileName();

    @Key("be.headless")
    boolean beHeadless();

}
