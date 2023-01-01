# Skeleton

Skeleton is a template generator tool for helping developers set up new projects quickly.

## Features

> **Note:** There are currently no features yet. This tool is in its early stages of development.

## Getting Started

This part shows how to obtain a copy of the project and get the tool running on your local machine.

### System requirement

This project depends on the tool below being available in your environment:

-  JDK 11

### Installation

> **Note:** Use PowerShell or Git Bash to execute commands on Windows.

1. Clone the repository to your local machine.

2. Navigate to the project directory.

3. Install the project as a distribution:

   ```shell
   ./gradlew installDist
   ```

4. Run the tool from the build directory:

   ```shell
   ./build/install/skeleton/bin/skeleton --help
   ```

Alternatively, skip steps 2 through 4 and open the project in [IntelliJ IDEA][1]. For more on how to build and run the app, please consult the [guide][2].

## License

This tool is available under the terms of the [GPL-3.0 license][3].

&copy; 2023 Mayeku Khisa.

[1]: https://www.jetbrains.com/idea/
[2]: https://www.jetbrains.com/help/idea/running-applications.html
[3]: LICENSE
