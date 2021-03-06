({
    baseUrl: "../resources/app/scripts",
    mainConfigFile: "../resources/app/scripts/main.js",
    name: "app",
    include: ["requireLib"],
    paths: {
        osirisConfig: "empty:" // this is loaded manually, and managed (by Puppet) outside the requireJs context
    },
    preserveLicenseComments: false,
    logLevel: 4,
    generateSourceMaps: true
})
