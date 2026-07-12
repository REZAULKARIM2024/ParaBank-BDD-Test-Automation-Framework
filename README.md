# ParaBank – BDD Test Automation Framework (Selenium · Cucumber · TestNG · RestAssured · Allure)

![Java](https://img.shields.io/badge/Java-11-orange) ![Selenium](https://img.shields.io/badge/Selenium-4.27.0-43B02A) ![Cucumber](https://img.shields.io/badge/Cucumber-7.18.0-23D96C) ![TestNG](https://img.shields.io/badge/TestNG-7.10.2-yellow) ![Maven](https://img.shields.io/badge/Build-Maven-C71A36) ![Tests](https://img.shields.io/badge/Tests-69%20UI%20%2B%2059%20API%2Fa11y%2Fperf-blue) ![Pass rate](https://img.shields.io/badge/Pass%20rate-100%25-brightgreen) ![Report](https://img.shields.io/badge/Report-Allure-orange)

A multi-layer test automation framework demonstrating real QA/SDET practice against a live, publicly shared banking application — UI/BDD, REST API, accessibility, and performance testing in one project, with 69 automated UI scenarios, 59 supporting API/accessibility/performance tests, and a running, dated ledger of confirmed live-application defects at its core.

## 🌐 Application Under Test

| Application | URL | Used for |
|---|---|---|
| ParaBank (Parasoft demo bank) | https://parabank.parasoft.com | Full BDD UI/E2E, REST API, accessibility & performance testing (live public demo — no local instance) |

## 📊 Latest Test Run

UI (Cucumber) suite — **69/69 scenarios, 100% passed** (17m 41s, 11 feature files), verified across multiple independent terminal runs.

| Metric | Result |
|---|---|
| ✅ Total UI scenarios (Cucumber) | 69 |
| ✅ Pass rate | 100% |
| ❌ Failed | 0 |
| ⏱️ Duration | 17m 41s |
| 🧩 Breakdown | 69 scenarios across 11 features, including 14 tagged `@knownIssue` — documented live-app defects, not framework bugs |
| 🖥️ Runtime | Java 11 (Maven `source`/`target`) · cucumber-jvm 7.18.0 · Windows (build 10.0.26200) |

API (46 default + 5 opt-in destructive), accessibility (7), and performance (6) run as separate TestNG suites — see **Test Coverage** below. All four suites write into the same `target/allure-results`, so running them back-to-back without `mvn clean` between runs and then calling `mvn allure:serve` unifies everything into one dashboard.

Numbers come from real terminal `mvn test` runs (`test-output/`, `target/surefire-reports/`) and the generated Cucumber/Allure reports — not a single cherry-picked run.

## 🚀 Overview

ParaBank is a live, publicly shared demo application maintained by Parasoft — not a sandbox under this project's control, and not artificially cooperative either. Three real engineering situations came up over the course of building this suite, and each was handled by diagnosing the actual cause rather than papering over it with a retry or a broader try/catch:

1. **WAF/bot defense, not a locator bug.** Several SQL-injection and XSS login payloads (`' OR '1'='1`, `<script>alert('xss')</script>`, etc.) got intercepted by a Cloudflare WAF/CDN challenge page in front of `parabank.parasoft.com`, before the request ever reached the ParaBank application itself. This was diagnosed from the actual response content (a Cloudflare challenge page, not the ParaBank login form) and logged and treated as a still-valid "malicious input rejected" outcome, instead of forcing a locator around it or silently failing.
2. **Flakiness removed at the source, not retried away.** Every browser session originally reused Chrome's default persistent profile. A successful login (or an auto-login registration) in one scenario left session cookies on disk, so the next scenario's fresh ChromeDriver process inherited a still-live session — six negative-login scenarios were producing false results because the browser believed it was already authenticated. Fixed by switching every browser (Chrome, Firefox, Edge) to incognito/private mode, so each scenario starts from a genuinely clean session.
3. **A running, dated ledger of confirmed defects vs. framework bugs.** 14 scenarios are tagged `@knownIssue`, each with a comment documenting the actual live ParaBank behavior confirmed on a real run — e.g. bill payments and fund transfers accepting negative amounts, contact-info updates accepting an empty first name, a very-long address value crashing the server outright. These aren't skipped: they're asserted *against* the real (defective) behavior, which makes them double as canaries. When ParaBank silently fixed six related login-bypass defects, those six scenarios started failing on their own on the next run — which is exactly what triggered reverting them back to standard, correct negative assertions.

Diagnose → decide → document — the difference between a suite that just turns green and one that tells you the truth about the system it's testing.

## 🛠️ Tech Stack

- **Language:** Java 11
- **Automation Tool:** Selenium WebDriver 4.27.0
- **BDD Framework:** Cucumber-JVM 7.18.0 (Gherkin)
- **Test Runner:** TestNG 7.10.2
- **API Testing:** RestAssured 5.5.0 + JSON Schema Validator
- **Accessibility:** axe-core (Selenium bindings) 4.9.1
- **Load/Performance:** Apache JMeter (template) + custom TestNG performance smoke checks
- **Reporting:** TestNG native reports, Cucumber HTML reports (built-in + `maven-cucumber-reporting` dashboard), Allure 2.29.0
- **Build Tool:** Maven
- **Driver Management:** WebDriverManager 5.9.2
- **Other:** Apache POI (Excel utilities)

## 📁 Project Structure

```
src/test/java/
  runners/            TestNGCucumberRunner (Cucumber entry point)
  stepdefinitions/    Cucumber step definitions - one class per feature
  pages/              Page Object Model classes
  api/                RestAssured API test classes (plain TestNG, no Cucumber)
  accessibility/      axe-core accessibility checks
  performance/        Performance smoke assertions
  utils/              DriverFactory, DiagnosticsUtils, ExcelUtils, PerformanceUtils

src/test/resources/
  features/           11 Gherkin .feature files (69 executable scenarios)
  allure.properties
  api.properties

performance/jmeter/
  ParaBank_Load_Test_Plan.jmx   JMeter template (load/stress/spike/endurance)
  README.md                     How to run and interpret it

testng.xml                 Default suite - runs the Cucumber UI tests
testng-api.xml              API test suite
testng-accessibility.xml    Accessibility test suite
testng-performance.xml      Performance smoke suite
pom.xml                     Maven dependencies, Allure + report plugins
README.md
```

## 🎯 Test Coverage (69 UI scenarios + 59 supporting tests)

| Layer | Type | Tool | Positive / Negative | Count |
|---|---|---|---|---|
| 🔥 UI Smoke | Happy-path scenario per feature (`@smoke`) | Selenium + Cucumber | + | 11 |
| 🧭 UI Regression | Full functional, negative, boundary & security flows | Selenium + Cucumber | +/− | 69 (across 11 features) |
| 🐞 Known live-app defects | Confirmed ParaBank defects, asserted & canary-tested | Cucumber `@knownIssue` | n/a | 14 (subset of the 69) |
| 🔌 API | Login, Accounts, Transactions, Customers, Loans, Positions, Contract-validation, Misc | RestAssured + TestNG | +/− | 46 by default (+5 opt-in `destructive`) |
| ♿ Accessibility | axe-core scans across key pages | Selenium + axe-core | +/− | 7 |
| ⚡ Performance | Page-load / response-time smoke budgets | Selenium + TestNG | +/− | 6 |
| 📈 Load/Stress/Spike/Endurance | JMeter Thread Group templates | Apache JMeter | — | scaffold (see `performance/jmeter/README.md`) |

UI Smoke (11) and Known live-app defects (14) are subsets of the 69 total UI scenarios, not additional tests. Every functional area has both positive and negative cases; login also includes a data-driven `Scenario Outline` covering five SQLi/XSS injection payloads.

## ⚙️ Framework Highlights

- ✔️ **BDD (Cucumber + Gherkin)** — human-readable scenarios, one feature file per user flow
- ✔️ **Page Object Model (POM)** — every locator lives in one page class, reused across step definitions
- ✔️ **Layered design** — runners · stepdefinitions · pages · api · accessibility · performance · utils
- ✔️ **Config-driven** — browser and headless mode via `-Dbrowser=` / `-Dheadless=`, suite selection via `-DsuiteFile=`, scenario filtering via `-Dcucumber.filter.tags=`
- ✔️ **Self-documenting failures** — `Hooks.java` auto-attaches a screenshot plus live page diagnostics (URL, title, visible error text) to the Cucumber report on any failure
- ✔️ **Flakiness controls** — incognito/private browser sessions per scenario, plus WAF-aware assertions for security-negative cases
- ✔️ **Confirmed-defect ledger** — the `@knownIssue` tagging pattern, with dated comments, doubling as regression canaries against the live application
- ✔️ **Triple-layer reporting** — TestNG native + Cucumber HTML (basic + polished dashboard) + Allure

## ▶️ Running the Tests

All commands run from this directory (the one containing `pom.xml`).

```bash
# Full UI (Cucumber) suite - 69 scenarios, default
mvn test

# Only the non-knownIssue scenarios (55 of 69) - a "clean/green" gate
mvn test -Dcucumber.filter.tags="not @knownIssue"

# Slice by tag
mvn test -Dcucumber.filter.tags="@smoke"
mvn test -Dcucumber.filter.tags="@regression"
mvn test -Dcucumber.filter.tags="@negative"

# Re-check documented known defects (a pass here means ParaBank fixed it -
# go remove the @knownIssue tag)
mvn test -Dcucumber.filter.tags="@knownIssue"

# Cross-browser
mvn test -Dbrowser=firefox
mvn test -Dbrowser=edge -Dheadless=true

# Other suites
mvn test -DsuiteFile=testng-api.xml
mvn test -DsuiteFile=testng-accessibility.xml
mvn test -DsuiteFile=testng-performance.xml
```

> Run from an actual terminal (`cmd` / PowerShell / bash), not an IDE's built-in "Run As" launcher — the IDE launcher bypasses `testng.xml` and can print Cucumber's raw machine-readable event stream to the console instead of the normal readable report.

## 📈 Reporting

| Report | What it shows | How to generate |
|---|---|---|
| **TestNG** (built-in) | Plain pass/fail results table | Automatic — `test-output/index.html`, `test-output/emailable-report.html` |
| **Cucumber HTML** | BDD scenario report with steps | Basic report automatic at `target/cucumber-report.html`; richer dashboard via `mvn net.masterthought:maven-cucumber-reporting:generate` → `target/cucumber-html-reports/overview-features.html` |
| **Allure** (recommended) | Unified interactive dashboard — overview, pass/fail charts, per-scenario steps and tags | Results auto-written to `target/allure-results` during `mvn test`; view with `mvn allure:serve` or build a static site with `mvn allure:report` |

Run the UI suite plus the API/accessibility/performance suites back-to-back (without `mvn clean` in between) to accumulate all results in `target/allure-results`, then run `mvn allure:serve` once for a single unified report.

## 📌 Prerequisites

- Java 11+
- Maven 3.6+
- Google Chrome (default browser; Firefox and Edge are also supported)
- Internet connection — tests run against the live public demo at `parabank.parasoft.com`, not a local instance
- (First run only) the Allure commandline tool, downloaded automatically by the `allure-maven` plugin

## 📈 Future Enhancements

- Add GitHub Actions CI to run the API/accessibility suites (the deterministic, non-live-UI-dependent ones) on every push
- Enable parallel execution across feature files
- Add a security (DAST) scan, e.g. an OWASP ZAP baseline pass against a staging deployment
- Expand the JMeter plan from a template into executed, reported Load/Stress/Spike/Endurance runs against a non-production target
- Data-driven scenarios from Excel/JSON (an `ExcelUtils` helper already exists as a starting point)

## 👨‍💻 Author

Rezaul Karim — QA Automation Engineer / SDET
📧 rknyc2021@gmail.com

## 📄 Summary

This framework goes beyond happy-path UI testing: it spans UI/BDD, API, accessibility, and performance, and backs every claim with a verified 100% pass rate on 69 UI scenarios (17m 41s) plus 59 supporting API/accessibility/performance tests. It also documents genuine engineering decisions made along the way — working around a live Cloudflare WAF challenge, removing session-cookie flakiness at the source, and maintaining a dated, self-correcting ledger of confirmed application defects rather than either ignoring them or letting them silently fail the build. Built to be readable, maintainable, and interview-ready.
