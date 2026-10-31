const { withSettingsGradle, withAppBuildGradle } = require('@expo/config-plugins');

function withWearOsSettings(config) {
  return withSettingsGradle(config, (config) => {
    const wearInclude = `
// Wear OS companion module
include ':wear'
project(':wear').projectDir = new File(rootProject.projectDir, '../../wear')
`;
    if (!config.modResults.contents.includes("include ':wear'")) {
      config.modResults.contents += wearInclude;
    }
    return config;
  });
}

function withWearOsAppBuildGradle(config) {
  return withAppBuildGradle(config, (config) => {
    const wearDep = `
    wearApp project(':wear')
`;
    if (!config.modResults.contents.includes("wearApp project(':wear')")) {
      config.modResults.contents = config.modResults.contents.replace(/dependencies\s*\{/, `dependencies {${wearDep}`);
    }
    return config;
  });
}

module.exports = function withWearOs(config) {
  config = withWearOsSettings(config);
  config = withWearOsAppBuildGradle(config);
  return config;
};
