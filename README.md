# Skeleton

Skeleton is a template generator tool that streamlines the project setup process for developers, enabling a faster and more efficient start.

## Features

-  **Template Listing**: View all templates at your disposal and find the perfect starting point for your projects.

-  **Configuration Management**: Adapt the tool to your local environment, ensuring it aligns with your specific setup and preferences.

-  **Project Generation**: Easily create new projects with predefined templates, accelerating the initial development stage.

## Getting Started

This section shows how to get the tool up and running on your local machine.

### System requirement

To use this tool, ensure the following software is installed on your system:

-  JRE or JDK 17 or newer

### Installation

1. Download the [latest release][1].

2. Extract the downloaded file (.zip or .tar.gz) to your preferred location.

3. Set the PATH variable:

   <details>
   <summary>Windows</summary>

   -  Right-click on "**This PC**" or "**My Computer**" and select "**Properties**".

   -  Go to "**Advanced system settings**" &rarr; "**Advanced**" tab &rarr; "**Environment Variables**".

   -  Under "**User variables**", find the "**Path**" variable. If it doesn't exist, create it by clicking "**New**", naming it "**Path**", and setting its value.

   -  If "**Path**" exists, select it then click "**Edit**" &rarr; "**New**" and add the absolute path to the `bin` directory of your extracted file.

   -  Click "**OK**" on each open window to save your changes.
   </details>

   <details>
   <summary>Linux and macOS</summary>

   -  Open a terminal and determine which shell you are using (commonly bash or zsh). You can find out by running `echo $SHELL`.

   -  Add the following line to your `~/.bashrc` (for bash) or `~/.zshrc` (for zsh):

      ```shell
      export PATH="$PATH:/path/to/bin"
      ```

      Replace `/path/to/bin` with the absolute path to the `bin` directory of your extracted file.

   -  Apply changes by running `source ~/.bashrc` (for bash) or `source ~/.zshrc` (for zsh). Alternatively, restarting your terminal will also effect the updates.
   </details>

4. Verify installation:

   ```shell
   skeleton --version
   ```

5. Follow the steps outlined [here][2] to set up your template collection.

### Creating a project

1. Execute `skeleton --list-templates` to view available templates and choose one that aligns with your project's needs. An overview of each template can be found [here][3].

2. Generate your project structure:

   ```shell
   skeleton create -t TEMPLATE DIRECTORY
   ```

   Replace `TEMPLATE` with the name of the template you've chosen, and `DIRECTORY` with the desired name for your project's directory.

   Run `skeleton create --help` for a full list of options available for this command.

3. Start your development journey...It's that simple!

## License

This tool is available under the terms of the [GPL-3.0 license][4].

&copy; 2023-2024 Mayeku Khisa.

[1]: https://github.com/mayekukhisa/skeleton/releases/latest
[2]: https://github.com/mayekukhisa/skeleton-templates#installation
[3]: https://github.com/mayekukhisa/skeleton-templates#available-templates
[4]: LICENSE
