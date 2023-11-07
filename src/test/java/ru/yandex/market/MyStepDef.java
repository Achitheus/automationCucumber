package ru.yandex.market;

import com.codeborne.selenide.*;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Step;
import io.qameta.allure.model.Status;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.ru.yandex.YandexMain;
import pages.ru.yandex.market.CategoryGoods;
import pages.ru.yandex.market.MarketMain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static helpers.AllureCustom.markOuterStepAsFailedAndStop;
import static helpers.Properties.testProperties;
import static io.qameta.allure.Allure.step;

public class MyStepDef {
    public static final Logger log = LoggerFactory.getLogger(MyStepDef.class);
    private final YandexMain yandexMain = page(YandexMain.class);
    private final MarketMain marketMain = page(MarketMain.class);
    private final CategoryGoods categoryGoods = page(CategoryGoods.class);

    @ParameterType("(.*)")
    public List<String> listString(String list) {
        return Arrays.asList(list.split(" *, *"));
    }

    public static String getCurrentLocation() {
        open("http://2ip.ru");
        SelenideElement acceptCookie = $(By.cssSelector(".notice__container__ok"));
        if(acceptCookie.exists()) {
            acceptCookie.click();
        }
        String currentLocation = $(By.id("ip-info-city")).getText();
        Selenide.closeWebDriver();
        return currentLocation;
    }

    public static String editedUserAgent() {
        open("http://github.com");
        String currentUserAgent = Selenide.getUserAgent();
        log.info("User agent supposed to change is: {}", currentUserAgent);
        String userAgent = currentUserAgent.replaceAll("(Headless)", "");
        log.info("User-Agent value can be used: {}", userAgent);
        Selenide.closeWebDriver();
        return userAgent;
    }

    @BeforeAll
    public static void beforeScenario() {
        log.info("Current active profile name is: {}", testProperties.activeProfileId());
        SelenideLogger.addListener("Allure Selenide", new AllureSelenide()
                .includeSelenideSteps(false));
        ChromeOptions options = new ChromeOptions();
        Configuration.browserCapabilities = options;
        Configuration.timeout = 6_000;
        Configuration.headless = testProperties.browserHeadless();
        if (testProperties.useSelenoid()) {
            Configuration.remote = "http://localhost:4444/wd/hub";
            log.info("Current location by IP is: {}", getCurrentLocation());
        }
        if (testProperties.browserHeadless()) {
            options.addArguments("--user-agent=" + editedUserAgent());
        } else {
            Configuration.browserSize = "1920x1080";
        }
        if (testProperties.useChromeProfile()) {
            options.addArguments("--user-data-dir=" + testProperties.chromeDir());
            options.addArguments("--profile-directory=" + testProperties.profileDir());
        }

    }

    @Step("Закрываю браузер")
    @After
    public void afterScenario() {
        Selenide.closeWebDriver();
    }

    @Given("перейти на сайт {string}")
    public void goToPage(String url) {
        log.info("Go to url: {}", url);
        open(url);
        log.info("Current user agent: " + Selenide.getUserAgent());
    }

    @Given("перейти в сервис {string}")
    public void перейтиВСервис(String serviceName) {
        yandexMain.goToService(serviceName);
    }

    @Given("в каталоге навести курсор на секцию {string}, кликнуть на категорию {string}")
    public void перейтиВКатегориюТоваров(String section, String category) {
        marketMain.toSectionCategory(section, category);
    }

    @When("фильтр {string} установлен значениями: {listString}")
    public void установитьФильтрПеречислений(String filterName, List<String> checkboxNames) {
        categoryGoods.setEnumFilter(filterName, new HashSet<>(checkboxNames));
    }

    @Then("все названия товаров содержат одно из ключевых слов: {listString}")
    public void всеНазванияТоваровСодержатОдноИзКлючСлов(List<String> checkWords) {
        int infinityCyclePreventer = 0;
        do {
            infinityCyclePreventer++;
            ElementsCollection productNameEls = categoryGoods.getPageProductNames();
            log.debug("Страница {}. Число товаров: {}", infinityCyclePreventer, productNameEls.size());
            log.trace("Названия товаров на {} странице: {}", infinityCyclePreventer, productNameEls.texts());

            SelenideElement badName = productNameEls.find(not(match("",
                    nameEl -> checkWords.stream().anyMatch(
                            brand -> nameEl.getText().toLowerCase().contains(brand.toLowerCase())))));
            boolean badNameExists = badName.exists();
            step("На стр. " + infinityCyclePreventer + " все названия товаров " +
                            "соответствуют фильтру \"Производитель\": " + checkWords,
                    badNameExists ? Status.FAILED : Status.PASSED);
            badName.shouldNot(exist.because("На стр. " + infinityCyclePreventer + " наименование товара \""
                    + (badNameExists ? badName.getText() : "") + "\" не соответствует фильтру \"Производитель\". " +
                    "Слова проверки: " + checkWords));
        } while (categoryGoods.nextPage() && infinityCyclePreventer < 1000);
        log.info("Обработано {} страниц товаров", infinityCyclePreventer);
    }

    @And("\"мягко\" проверить, что город, определенный сервисом, {string}")
    public void проверитьЧтоГородОпределенныйСервисом(String city) {
        String actualCity = categoryGoods.getCity().getText();
        boolean cityIsCorrect = city.equals(actualCity);
        step("Ожидался город: \"" + city + "\", а по факту: " + actualCity,
                cityIsCorrect ? Status.PASSED : Status.FAILED);
        if (!cityIsCorrect) {
            markOuterStepAsFailedAndStop();
        }

    }
}