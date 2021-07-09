[![Published on Vaadin Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/chip-combobox-for-vaadin)
[![Latest version](https://img.shields.io/maven-central/v/com.xdev-software/vaadin-chip-combobox)](https://mvnrepository.com/artifact/com.xdev-software/vaadin-chip-combobox)
[![Build](https://img.shields.io/github/workflow/status/xdev-software/vaadin-chip-combobox/Check%20Build/develop)](https://github.com/xdev-software/vaadin-chip-combobox/actions/workflows/checkBuild.yml?query=branch%3Adevelop)
![Vaadin 14+](https://img.shields.io/badge/Vaadin%20Platform/Flow-14+-00b4f0.svg)


## vaadin-chip-combobox
A ComboBox with Chips/Chip Components for Vaadin Flow

![demo](assets/demo.png)
![demo2](assets/demo2.png)

## Installation
[Installation guide for the latest release](https://github.com/xdev-software/vaadin-chip-combobox/releases/latest#Installation)


## Run the Demo
1. Checkout the repo
2. Run ``mvn clean install``
3. Navigate into ``vaadin-chip-combobox-demo``
4. Run ``mvn jetty:run``
5. Open http://localhost:8080

<details>
   <summary>Show example</summary>

   ![demo](assets/demo.gif)
</details>

## Releasing [![Build](https://img.shields.io/github/workflow/status/xdev-software/vaadin-chip-combobox/Release?label=Release)](https://github.com/xdev-software/vaadin-chip-combobox/actions/workflows/release.yml)
If the ``develop`` is ready for release, create a pull request to the ``master``-Branch and merge the changes

When the release is finished do the following:
* Merge the auto-generated PR (with the incremented version number) back into the ``develop``
* Add the release notes to the [GitHub release](https://github.com/xdev-software/vaadin-chip-combobox/releases/latest)
* Upload the generated release asset zip into the [Vaadin Directory](https://vaadin.com/directory)

## Dependencies and Licenses
View the [license of the current project](LICENSE) or the [summary including all dependencies](https://xdev-software.github.io/vaadin-chip-combobox/dependencies/)
