

import groovy.json.JsonSlurper;

class Github {

    String baseUrl;

    final authToken;

    public Github (String authToken) {
        this.authToken = this.authToken;
        this.baseUrl = "https://api.github.com";
    }

    public Github (String authToken, String baseUrl) {
        this.authToken = authToken;
        this.baseUrl = baseUrl;
    }

    public Github () {
        this.authToken = "";
        this.baseUrl = "https://api.github.com";
    }


    public getProjects(org) {
        return this.getRepos(org).collect {
            print "analyzing repo: "
            print it
            print "\n"
            it.branches = this.getBranches(org, it);
            return it;
        };
    }


    private List<String> getBranches (org, repositoryObj) {
        return this.apiCall("repos/${org}/${repositoryObj.name}/branches")
                .collect { it.name }
                .findAll{ it.contains("feature") || it.contains("defect") }
    }

    private getRepos (org) {
        return this.apiCall("orgs/${org}/repos")
                .collect { analyzeRepo(org, it.name) }
                .findAll{ it.enabled };
    }

    private analyzeRepo (String org, repo) {
        try {
            def sl = slurp("https://raw.githubusercontent.com/${prg}/${repo}/master/butler.json");
            sl.org = org;
            sl.repo = repo;
            return sl;
        }
        catch(e) {
            return [
                org : org,
                name : repo,
                enabled : false
            ];
        }
    }

    public apiCall(GString urlString) {
        return slurp("${this.baseUrl}/${urlString}");
    }

    public slurp (GString urlStr) {
        def url = new URL(urlStr)
        def connection = (HttpURLConnection)url.openConnection()
        connection.setRequestMethod("GET")
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Authorization", "token ${this.authToken}")
        return new JsonSlurper().parse(connection.getInputStream())
    }

}

