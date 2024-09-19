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

        async init() {
            this.listTestMethods();
        },

        async listTestMethods() {
            try {
                const urlSearchParams = new URLSearchParams(window.location.search);
                this.package = urlSearchParams.get('package') || 'example'; // 'example' is default
                this.listType = urlSearchParams.get('listType') || 'classes'; // 'classes' is default
                this.availableTestMethods = await (await fetch('api/list-test-methods?package=' + this.package + '&listType=' + this.listType)).json();
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
                const response = await fetch('api/run-test-methods', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        testMethods: this.selectedTestMethods.map(tm => tm['fullyQualifiedMethodName'])
                    })
                })
                const data = await response.json();
                console.log(data)
            } catch (err) {
                console.log("error running test methods: " + err)
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
            this.availableFilteredMethods = this.search(this.availableTestMethods, this.availableFilterKeyword);
            this.availableCheckedIds = [];
        },

        filterSelectedTestMethods() {
            this.selectedFilteredMethods = this.search(this.selectedTestMethods, this.selectedFilterKeyword);
            this.selectedCheckedIds = [];
        },

        search(fullArray, q) {
            const keywords = q.trim().split(/\s+/);
            console.log(keywords);
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

//            if (checkedItems.length > 0) {
//                // add the checkedItems to the selectedTestMethods array, exclude those already in the selectedTestMethods array
//                this.selectedTestMethods = this.selectedTestMethods.filter(stm => !checkedItems.includes(stm))
//                checkedItems.forEach(item => {
//                    this.selectedTestMethods.push(item);
//                    // remove the checkedItems from the availableTestMethods array
//                    this.availableTestMethods = this.availableTestMethods.filter(atm => atm['fullyQualifiedMethodName'] !== item['fullyQualifiedMethodName'])
//                })
//            }