document.addEventListener('alpine:init', () => {
    Alpine.data('testMethodData', () => ({
        package: '',
        availableTestMethods: [],
        availableCheckedIds: [],

        selectedTestMethods: [],
        selectedCheckedIds: [],

        async init() {
            this.listTestMethods();
        },

        async listTestMethods() {
            try {
                this.package = new URLSearchParams(window.location.search).get('package') || 'example'; // 'example' is default
                this.availableTestMethods = await (await fetch('api/list-test-methods?package=' + this.package)).json();
                const url = new URL(window.location)
                url.searchParams.set('package', this.package);
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
            }
        },

        removeFromSelectedTestMethods() {
            if (this.selectedCheckedIds.length > 0) {
                [this.selectedTestMethods, this.availableTestMethods, this.selectedCheckedIds] = this.moveFromTo(this.selectedTestMethods, this.availableTestMethods, this.selectedCheckedIds);
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