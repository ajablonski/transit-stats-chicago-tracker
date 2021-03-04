module.exports = require('./scalajs.webpack.config');

module.exports.module.rules = [
    {
        test: /\.js$/,
        enforce: "pre",
        use: ["scalajs-friendly-source-map-loader"]
    }
]
