package com.accountbook;

import com.accountbook.view.MainView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SpringBootApplication
public class ApplicationStarter extends Application {

    static Logger logger = LoggerFactory.getLogger(ApplicationStarter.class);

    private static ConfigurableApplicationContext springContext;

    public static final Executor contextAwareExecutor = (command ) -> {
        // Capture current thread context
        Map< String, String > contextMap = ThreadContext.getContext( );
        Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread( runnable );
            thread.setName( "SpringBoot-Startup" );
            thread.setDaemon( false );
            return thread;
        } ).execute( ( ) -> {
            // Restore context in new thread
            ThreadContext.putAll( contextMap );
            try {
                command.run( );
            } finally {
                ThreadContext.clearAll( );
            }
        } );
    };

    public static void main(String[] args) {
        try {
            Class.forName( "javafx.application.Application" );
            System.setProperty( "java.awt.headless", "false" );

            CompletableFuture<ConfigurableApplicationContext> springFuture = CompletableFuture
                    .supplyAsync( ( ) -> {
                        try {
                            logger.info( "Initializing Spring Boot context..." );
                            ConfigurableApplicationContext context = SpringApplication.run( ApplicationStarter.class, args );
                            logger.info( "Spring Boot context initialized successfully" );
                            return context;
                        } catch ( Exception ex ) {
                            logger.error( "Failed to start Spring Boot application", ex );
                            throw new RuntimeException( ex );
                        }
                    }, contextAwareExecutor );

            // Store the future for later use
            springFuture.thenAccept( context -> {
                        ApplicationStarter.springContext = context;
                        logger.info( "Spring context available for JavaFX" );
                    } )
                    .exceptionally( throwable -> {
                        logger.error( "Spring Boot startup failed", throwable );
                        return null;
                    } );

            // Launch JavaFX
            logger.info( "Launching JavaFX application..." );
            Application.launch( args );

        } catch ( ClassNotFoundException ex ) {
            final String msg = "JavaFX not available, running as web application only...";
            logger.info( msg );
            System.out.println( msg );
            SpringApplication.run( ApplicationStarter.class, args );
        } catch ( Exception e ) {
            logger.error( "Application startup failed", e );
            System.exit( 1 );
        }

    }

    @Override
    public void start(Stage primaryStage ) throws Exception {
        try {
            // Wait for Spring Boot to start
            waitForSpringBoot( );

            MainView.configureStage( primaryStage, "" /*add webpage url if using webview*/, springContext );

            // Show the application
            primaryStage.show( );

        } catch ( Exception e ) {
            logger.error(String.valueOf(e));
            System.err.println( "Failed to start desktop application: " + e.getMessage( ) );
            Platform.exit( );
        }
    }

    @Override
    public void stop( ) throws Exception {
        logger.info( "Shutting down application..." );
        if ( springContext != null ) {
            springContext.close( );
        }
        super.stop( );
    }

    private void waitForSpringBoot( ) {
        System.out.println( String.format( "%s - Waiting for Spring Boot to start...", LocalDateTime.now( ) ) );
        int maxAttempts = 30;
        int sleep_millis = 1000;
        int attempts = 0;

        while ( attempts < maxAttempts ) {
            try {
                if ( springContext != null && springContext.isRunning( ) ) {
                    Thread.sleep( sleep_millis );
                    System.out.println( "Spring Boot started successfully!" );
                    return;
                }
                Thread.sleep( sleep_millis );
                attempts++;
                System.out.println( String.format( "%s - Still waiting for Spring Boot to start... Attempt: %d", LocalDateTime.now( ), attempts ) );
            } catch ( InterruptedException e ) {
                logger.error(String.valueOf(e));
                Thread.currentThread( ).interrupt( );
                throw new RuntimeException( "Interrupted while waiting for Spring Boot", e );
            }
        }
        throw new RuntimeException( String.format( "Spring Boot failed to start after completing %d attempts within the timeout period %d second(s)", attempts, ( ( sleep_millis * maxAttempts ) / 1000 ) ) );
    }
}