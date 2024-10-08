document.addEventListener('alpine:init', () => {
    Alpine.data('testMethodData', () => ({
        package: '',
        listType: '',
        availableTestMethods: [],
        availableFilteredMethods: [],
        availableFilterKeyword: '',
        availableCheckedIds: [],

        isEditMode: false,
        selectedTestMethods: [],
        selectedFilteredMethods: [],
        selectedFilterKeyword: '',
        selectedCheckedIds: [],

        isRunning: false,
        report: null,

        async init() {
            this.listTestMethods();
        },

        async listTestMethods() {
            try {
                const urlSearchParams = new URLSearchParams(window.location.search);
                this.package = urlSearchParams.get('package') || null;
                this.listType = urlSearchParams.get('listType') || 'classes'; // 'classes' is default
                console.log("listType", this.listType)
                var requestUrl = 'api/list-test-methods?listType=' + this.listType;
                if (this.package) {
                    requestUrl = 'api/list-test-methods?package=' + this.package + '&listType=' + this.listType;
                }
                const resp = await (await fetch(requestUrl)).json();
                this.availableTestMethods = resp.availableTestItems.sort((a, b) => a.fullyQualifiedMethodName.localeCompare(b.fullyQualifiedMethodName)); // sort by fullyQualifiedMethodName;
                this.package = resp.package; // get package from response
                console.log("this.availableTestMethods", this.availableTestMethods)
                this.availableFilteredMethods = this.availableTestMethods;
                const url = new URL(window.location)
                url.searchParams.set('package', this.package);
                url.searchParams.set('listType', this.listType);
                window.history.replaceState(null, '', url.toString())
            } catch (err) {
                console.log("error loading test methods from: " + this.package, err)
            }
        },

        async runTestMethods() {
            try {
                this.isRunning = true;
                this.report = null;
                const response = await fetch('api/run-test-methods', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        testMethods: this.selectedTestMethods.map(tm => tm['fullyQualifiedMethodName'])
                    })
                })
                this.report = await response.json();
                this.report.runReportItems.sort((a, b) => a.testItem.fullyQualifiedMethodName.localeCompare(b.testItem.fullyQualifiedMethodName));
                // for each item in report.runReportItems, add a 'hidden' property and set to true if the 'exception' property is available
                this.report.runReportItems.forEach(item => item['hidden'] = item['exception'] != null);
                console.log("this.report", this.report)
                this.isRunning = false;
            } catch (err) {
                console.log("error running test methods: " + err)
                this.isRunning = false;
            }
        },

        addToSelectedTestMethods() {
            if (this.availableCheckedIds.length > 0) {
                [this.availableTestMethods, this.selectedTestMethods, this.availableCheckedIds] = this.moveFromTo(this.availableTestMethods, this.selectedTestMethods, this.availableCheckedIds);

                this.availableFilteredMethods = this.availableTestMethods;
                this.selectedFilteredMethods = this.selectedTestMethods; // reset the selected filtered list

                this.availableFilterKeyword = '';
                this.selectedFilterKeyword = '';
                this.filterAvailableTestMethods(); // trigger to refresh the available filtered list
                this.filterSelectedTestMethods(); // trigger to refresh the selected filtered list
            }
        },

        removeFromSelectedTestMethods() {
            if (this.selectedCheckedIds.length > 0) {
                [this.selectedTestMethods, this.availableTestMethods, this.selectedCheckedIds] = this.moveFromTo(this.selectedTestMethods, this.availableTestMethods, this.selectedCheckedIds);

                this.availableFilteredMethods = this.availableTestMethods;
                this.selectedFilteredMethods = this.selectedTestMethods; // reset the selected filtered list

                this.availableFilterKeyword = '';
                this.selectedFilterKeyword = '';
                this.filterAvailableTestMethods(); // trigger to refresh the available filtered list
                this.filterSelectedTestMethods(); // trigger to refresh the selected filtered list
            }
        },

        moveFromTo(fromArray, toArray, ids) {
            if (ids.length > 0) {
                toArray = [...new Set(toArray.concat(fromArray.filter(item => ids.includes(item['fullyQualifiedMethodName']))))];
                ids.forEach(i => {
                    fromArray = fromArray.filter(fromItem => fromItem['fullyQualifiedMethodName'] !== i)
                });
                return [fromArray, toArray, []]
            }
        },

        filterAvailableTestMethods() {
            this.availableFilteredMethods = this.search(this.availableTestMethods, this.availableFilterKeyword).sort((a, b) => a.fullyQualifiedMethodName.localeCompare(b.fullyQualifiedMethodName));
            this.availableCheckedIds = [];
        },

        filterSelectedTestMethods() {
            this.selectedFilteredMethods = this.search(this.selectedTestMethods, this.selectedFilterKeyword).sort((a, b) => a.fullyQualifiedMethodName.localeCompare(b.fullyQualifiedMethodName));
            this.selectedCheckedIds = [];
        },

        search(fullArray, q) {
            const keywords = q.trim().split(/\s+/);
            console.log("keywords", keywords);
            return fullArray.filter(item => keywords.every(keyword => item['fullyQualifiedMethodName'].toLowerCase().indexOf(keyword.toLowerCase()) >= 0))
        },

        toggleAllAvailableFilteredMethods(isChecked) {
            if (isChecked) {
                this.availableCheckedIds = this.availableFilteredMethods.map(tm => tm['fullyQualifiedMethodName']);
            } else {
                this.availableCheckedIds = [];
            }
        },

        toggleAllSelectedFilteredMethods(isChecked) {
            if (isChecked) {
                console.log("checked");
                this.selectedCheckedIds = this.selectedFilteredMethods.map(tm => tm['fullyQualifiedMethodName']);
            } else {
                console.log("unchecked");
                this.selectedCheckedIds = [];
            }
        }
    }))

})
