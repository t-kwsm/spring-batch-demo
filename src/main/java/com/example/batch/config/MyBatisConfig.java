package com.example.batch.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis設定クラス
 * MyBatisのSqlSessionFactoryとMapperスキャンの設定を行います
 */
@Configuration
@MapperScan("com.example.batch.mapper")
public class MyBatisConfig {
    
    /**
     * SqlSessionFactoryの設定
     * 
     * @param dataSource データソース
     * @return SqlSessionFactory
     * @throws Exception 設定エラー時の例外
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        // Mapperファイルの場所を指定
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
        
        // エンティティパッケージを指定
        sessionFactory.setTypeAliasesPackage("com.example.batch.entity");
        
        // MyBatisの設定
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setDefaultFetchSize(100);
        configuration.setDefaultStatementTimeout(30);
        configuration.setCacheEnabled(false);
        configuration.setLocalCacheScope(org.apache.ibatis.session.LocalCacheScope.STATEMENT);
        
        sessionFactory.setConfiguration(configuration);
        
        return sessionFactory.getObject();
    }
}