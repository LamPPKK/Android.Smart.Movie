config.resolve = config.resolve || {};
config.resolve.fallback = Object.assign({}, config.resolve.fallback, {
    os: false,
    path: false,
});

// Compose's generated production config enables source-map-loader and a full
// source map over the complete Kotlin/Skiko graph. That graph is already
// compiled and source maps are not consumed by the static release host; the
// extra pass can make JS packaging take several minutes or exhaust CI time.
if (Array.isArray(config.module?.rules)) {
    config.module.rules = config.module.rules.filter((rule) => !JSON.stringify(rule.use || "").includes("source-map-loader"));
}
config.devtool = false;
// The Kotlin compiler already emits a production executable. Webpack's second
// minification pass is disproportionately expensive for the generated graph;
// keep the valid production bundle deterministic and let the browser cache
// handle the resulting static asset.
config.optimization = Object.assign({}, config.optimization, { minimize: false });
