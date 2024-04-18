import jenkins.model.Jenkins

// Create the Tools folder
folder('Tools') {
    description('Folder for miscellaneous tools.')
    displayName('Tools')
}

// Create the clone-repository job
freeStyleJob('Tools/clone-repository') {
    wrappers {
        preBuildCleanup()
    }
    parameters {
        stringParam('GIT_REPOSITORY_URL', '', 'Git URL of the repository to clone')
    }
    scm {
        git('$GIT_REPOSITORY_URL')
    }
    triggers {
        scm('H/1 * * * *')
    }
    steps {
        shell('git clone $GIT_REPOSITORY_URL')
    }
}

// Create the SEED job
freeStyleJob('Tools/SEED') {
    parameters {
        stringParam('GITHUB_NAME', '', 'GitHub repository owner/repo_name')
        stringParam('DISPLAY_NAME', '', 'Display name for the job')
    }
    steps {
        systemGroovyCommand('''
            import jenkins.model.Jenkins
            import hudson.model.*
            
            // Create a new job with the specified display name
            def jobName = build.buildVariableResolver.resolve('DISPLAY_NAME')
            def job = Jenkins.instance.createProjectFromTemplate(FreeStyleProject, jobName, Jenkins.instance.getItem('SEED'))
            
            // Set the GitHub project property for the new job
            def githubUrl = "https://github.com/${build.buildVariableResolver.resolve('GITHUB_NAME')}"
            job.properties.add(new GithubProjectProperty(githubUrl))
            
            // Save the changes
            job.save()
        ''')
    }
}