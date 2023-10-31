package ru.yandex.market;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
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
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.ru.yandex.YandexMain;
import pages.ru.yandex.market.CategoryGoods;
import pages.ru.yandex.market.MarketMain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.codeborne.selenide.Condition.match;
import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.page;
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

    @BeforeAll
    public static void beforeScenario() {
        log.info("Current active profile name is: " + testProperties.activeProfileName());
        SelenideLogger.addListener("Allure Selenide", new AllureSelenide()
                .includeSelenideSteps(false));
        ChromeOptions options = new ChromeOptions();
        Configuration.browser = "chrome";
        Configuration.timeout = 6_000;
        Configuration.browserSize = "1920x1080";
        Configuration.holdBrowserOpen = true;
        Configuration.browserCapabilities = options;
        Configuration.headless = testProperties.beHeadless();
        if(testProperties.useChromeProfile()) {
            options.addArguments("--user-data-dir=" + testProperties.chromeDir());
            options.addArguments("--profile-directory=" + testProperties.profileDir());
        }

    }

    @Step("Закрываю браузер")
    @After
    public void afterScenario() {
        Selenide.closeWindow();
    }

    @Given("перейти на сайт {string}")
    public void goToPage(String url) {
        open(url);
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
            if(badNameExists) {
                Assertions.fail("На стр. " + infinityCyclePreventer + " наименование товара \""
                        + badName.getText() + "\" не соответствует фильтру \"Производитель\". " +
                        "Слова проверки: " + checkWords
                );
            }
        } while (categoryGoods.nextPage() && infinityCyclePreventer < 1000);
        log.info("Обработано {} страниц товаров", infinityCyclePreventer);
    }

    @And("\"мягко\" проверить, что город, определенный сервисом, {string}")
    public void проверитьЧтоГородОпределенныйСервисом(String city) {
        String actualCity = categoryGoods.getCity().getText();
        boolean cityIsCorrect = city.equals(actualCity);
        step("Ожидался город: \"" + city + "\", а по факту: " + actualCity,
                cityIsCorrect ? Status.PASSED : Status.FAILED);
        if(!cityIsCorrect){
            markOuterStepAsFailedAndStop();;
        }

    }
}