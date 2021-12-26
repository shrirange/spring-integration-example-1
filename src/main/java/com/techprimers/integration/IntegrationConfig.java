package com.techprimers.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.DirectoryScanner;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.RecursiveDirectoryScanner;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;

import java.io.File;
import java.util.Arrays;

@Configuration
public class IntegrationConfig {

    @Autowired
    private Transformer transformer;

    @Bean
    public IntegrationFlow integrationFlow( MessageSource<File> fileReader) {
        return IntegrationFlows.from(fileReader,
                spec -> spec.poller(Pollers.fixedDelay(500)))
                //.transform(transformer, "transform")
                .handle(fileWriter())
                .get();
    }

    @Bean
    public FileWritingMessageHandler fileWriter() {
        FileWritingMessageHandler handler = new FileWritingMessageHandler(
                new File("destination")
        );
        handler.setExpectReply(false);
        return handler;
    }

    @Bean
    public FileReadingMessageSource fileReader(DirectoryScanner directoryScanner) {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(new File("source"));
        source.setScanner(directoryScanner);
        return source;
    }

    
    @Bean
    public DirectoryScanner directoryScanner(@Value("${inbound.filename.regex}") String regex) {
        DirectoryScanner scanner = new RecursiveDirectoryScanner();
        CompositeFileListFilter<File> filter = new CompositeFileListFilter<>(
                Arrays.asList(new AcceptOnceFileListFilter<>(),
                        new RegexPatternFileListFilter(regex))
        );
        scanner.setFilter(filter);
        return scanner;
    }
}
