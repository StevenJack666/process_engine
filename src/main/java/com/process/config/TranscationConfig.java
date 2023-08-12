package com.process.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;


@Configuration  //声明该类是核心配置类
@ComponentScan("com.process.mapper")  //开启spring注解扫描
@PropertySource("classpath:application.properties")  //引入properties文件
@MapperScan("com.process.mapper")   //MyBatis扫描dao接口

public class TranscationConfig {

    @Configuration  //声明该类是核心配置类
    @ComponentScan("com.process.mapper")  //开启spring注解扫描
    @PropertySource("classpath:application.properties")  //引入properties文件
    @MapperScan("com.process.mapper")   //MyBatis扫描dao接口
    public class Application {
        //定义属性 为属性注入数据（数据的来源上面引入的db.properties文件）
        @Value("${spring.datasource.driver-class-name}")
        private String driverClass;
        @Value("${spring.datasource.url}")
        private String url;
        @Value("${spring.datasource.name}")
        private String username;
        @Value("${spring.datasource.password}")
        private String password;

        //创建数据源返回数据源，Spring会自动调用该方法，并将该对象交给IOC容器管理
        @Bean
        public DataSource dataSource() {
            DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setDriverClassName(driverClass);
            druidDataSource.setUrl(url);
            druidDataSource.setUsername(username);
            druidDataSource.setPassword(password);
            return druidDataSource;
        }

        //创建SqlSessionFactoryBean对象,设置形参，Spring会自动去调用IOC容器中已有的数据源
        @Bean
        public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource) {
            SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
            sqlSessionFactoryBean.setDataSource(dataSource);
            return sqlSessionFactoryBean;
        }

    }


}
