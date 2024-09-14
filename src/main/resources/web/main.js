document.addEventListener('alpine:init', () => {
    Alpine.data('testMethodData', () => ({
        testMethods: [],
        package: 'example',
        checkedTestMethods: [],

        async init() {
            listTestMethods();
        },

        async listTestMethods() {
            try {
                this.testMethods = await (await fetch('api/list-test-methods?package=' + this.package)).json();
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
                        testMethods: this.checkedTestMethods
                    })
                })
                const data = await response.json();
                console.log(data)
            } catch (err) {
                console.log("error running test methods: " + err)
            }
        }

    }))

})