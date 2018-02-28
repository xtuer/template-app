// https://eslint.org/docs/user-guide/configuring

module.exports = {
    root: true,
    parserOptions: {
        parser: 'babel-eslint'
    },
    env: {
        browser: true,
    },
    // https://github.com/vuejs/eslint-plugin-vue#priority-a-essential-error-prevention
    // consider switching to `plugin:vue/strongly-recommended` or `plugin:vue/recommended` for stricter rules.
    extends: ['plugin:vue/essential', 'airbnb-base', 'eslint:recommended', 'plugin:vue/recommended'
    ],
    // required to lint *.vue files
    plugins: [
        'vue'
    ],
    // check if imports actually resolve
    settings: {
        'import/resolver': {
            webpack: {
                config: 'build/webpack.base.conf.js'
            }
        }
    },
    // add your custom rules here
    rules: {
        // don't require .vue extension when importing
        'import/extensions': ['error', 'always', {
            js: 'never',
            vue: 'never'
        }],
        // disallow reassignment of function parameters
        // disallow parameter object manipulation except for specific exclusions
        'no-param-reassign': ['error', {
            props: true,
            ignorePropertyModificationsFor: [
                'state', // for vuex state
                'acc', // for reduce accumulators
                'e' // for e.returnvalue
            ]
        }],
        // allow optionalDependencies
        'import/no-extraneous-dependencies': ['error', {
            optionalDependencies: ['test/unit/index.js']
        }],
        // allow debugger during development
        'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
        'indent': 0,
        'no-console': 0,
        'no-undef': 0,
        'no-alert': 0,
        'no-trailing-spaces': 0,
        'object-shorthand': 0,
        'func-names': 0,
        'prefer-arrow-callback': 0,
        'space-before-function-paren': 0,
        'no-unused-vars': 0,
        'comma-dangle': 0,
        'import/prefer-default-export': 0,
        'no-new': 0,
        'no-multi-spaces': 0,
        'global-require': 0,
        'import/no-dynamic-require': 0,
        'no-underscore-dangle': 0,
        'array-bracket-spacing': 0,
        'vue/html-self-closing': 0,
        'vue/html-indent': 0,
        'vue/max-attributes-per-line': 0,
        'vue/order-in-components': 0,
        'vue/html-end-tags': 0,
        'vue/no-parsing-error': 0,
        'vue/require-default-prop': 0,
        'vue/require-prop-types': 0
    }
};
