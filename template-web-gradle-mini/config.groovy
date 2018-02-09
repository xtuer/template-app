// gradle clean build -Denv=production
environments {
    development {
        database {
            driverClassName = 'com.mysql.jdbc.Driver'
            url = 'jdbc:mysql://localhost:3306/test?useUnicode=true&amp;characterEncoding=UTF-8'
            username = 'root'
            password = 'root'
        }

        logDir = "/temp/logs"
        staticPath =""
        thymeleafCacheable = false
    }

    production {
        database {
            driverClassName = 'com.mysql.jdbc.Driver'
            url = 'jdbc:mysql://localhost:3306/test?useUnicode=true&amp;characterEncoding=UTF-8'
            username = 'root'
            password = 'huaxia-123'
        }

        logDir = "/temp/logs"
        staticPath =""
        thymeleafCacheable = true
    }
}
