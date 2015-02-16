This directory contains a autoformat pre-commit script for git which will automatically format any staged files
ending in `.scala` before you commit.

# Installing

If you'd like to have the autoformat script used by default in all new projects (recommended), follow the
instructions below. If you want to manually enable it on a per-project basis, see the section
"Installing & Enabling on a Single Project"

## Setup a Template Project Directory

### TL;DR

If you're in a rush and like to walk on the wild side, just run the following command.

```mkdir -p ~/.git_template/hooks; curl s3-us-west-2.amazonaws.com/ai2-misc/autoformat.tar | tar xvf - -C ~/.git_template/hooks; git config --global init.templatedir ~/.git_template```

### Full Instructions

This assumes you haven't already set up a template project with a pre-commit hook. If you already have a pre-commit hook, you'll have to integrate the autoformat hook manually.

Git lets you define a project template that will be used when you run `git init`. This template will be applied to
the `.git` directory of the project. To create a template directory `~/.git_template` and configure git to use it,
run:
```
$ mkdir -p ~/.git_template/hooks
$ git config --global init.templatedir ~/.git_template
```

## Download & Install the Autoformatter

Download the pre-built autoformatter tar file [from S3](https://s3-us-west-2.amazonaws.com/ai2-misc/autoformat.tar).

Untar it into the template directory you created above:
```
$ tar xvf ~/Downloads/autoformat.tar -C ~/.git_template/hooks/
x pre-commit
x scalariform.jar
```

You may wish to verify that `pre-commit` script was extracted as executable:
```
$ ls -l ~/.git_template/hooks/pre-commit
-rwxr-xr-x  1 jkinkead  staff  3022 Jan 13 12:39 .git/hooks/pre-commit
```

## Enable Autoformatting on Existing Projects

Autoformatting will be enabled on all new projects by default. To enable on a project that's already been
initialized, just run `git init` in the repository:
```
$ git init
Reinitialized existing Git repository in /home/jkinkead/existing_repo/.git/
$ ls -l .git/hooks/pre-commit
-rwxr-xr-x  1 jkinkead  staff  3022 Jan 13 12:39 .git/hooks/pre-commit
```

You should be ready to go!


## Installing & Enabling on a Single Project

If you wish to enable manually on a single project, without having new projects auto-format, all you have to do
is [download the tar](https://s3-us-west-2.amazonaws.com/ai2-misc/autoformat.tar) and untar it into `.git/hooks`:
```
$ cd existing_repo
$ tar xvf ~/Downloads/autoformat.tar -C .git/hooks/
x pre-commit
x scalariform.jar
```

# Usage

By default, this script runs in auto-format mode. This will run scalariform on all of your staged files, and abort
the commit if any are changed, printing out the file paths with changes. In this mode, an error will be printed out if you have unstaged changes
to a staged file (if you've edited a file since you `git add`-ed it). This is because the scalariform changes are
left unstaged, and requiring no unstaged changes means that you can safely run `git checkout -- <file>` to
revert the scalariform updates. 

You can also run the pre-commit script in validate ("nag") mode, which you can enable by setting `hooks.autoformat`
to `false`:
```
git config --global hooks.autoformat false
```
In this mode, staged files are checked for format errors, and the commit is aborted if any of the files contain
errors. File paths with errors are printed.

To skip all pre-commit checks (including the autoformatter), you can always run `git commit --no-verify`.
