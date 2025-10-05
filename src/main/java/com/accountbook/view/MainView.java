package com.accountbook.view;

import com.accountbook.ApplicationStarter;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ConfigurableApplicationContext;


@Log4j2
public final class MainView {


    private static VBox createMainView( String mainPage ) {

        // Create the root VBox
        VBox root = new VBox( );

        // Create AnchorPane
        AnchorPane anchorPane = new AnchorPane( );
        anchorPane.setMaxHeight( -1.0 );
        anchorPane.setMaxWidth( -1.0 );
        anchorPane.setPrefHeight( -1.0 );
        anchorPane.setPrefWidth( -1.0 );
        VBox.setVgrow( anchorPane, Priority.ALWAYS );

        /*// Create WebView
        WebView webView = new WebView( );
        webView.setContextMenuEnabled( true );

        // Create WebEngine
        WebEngine webEngine = webView.getEngine( );
        webEngine.setJavaScriptEnabled( true );
        webEngine.setUserStyleSheetLocation( ApplicationStarter.class.getResource( "/static/css/webview.css" ).toString( ) );
        webEngine.getLoadWorker( )
                .stateProperty( )
                .addListener(
                        ( observable, oldValue, newValue ) -> {
                            if ( newValue == Worker.State.SUCCEEDED ) {
                                System.out.println( "Page loaded successfully" );
                            } else if ( newValue == Worker.State.FAILED ) {
                                System.err.println( "Failed to load page" );
                            }
                        } );
        webEngine.load( mainPage );*/

        // Set AnchorPane constraints for WebView
    /*    AnchorPane.setBottomAnchor( webView, 0.0 );
        AnchorPane.setLeftAnchor( webView, 0.0 );
        AnchorPane.setRightAnchor( webView, 0.0 );
        AnchorPane.setTopAnchor( webView, 0.0 );

        // Add WebView to AnchorPane
        anchorPane.getChildren( ).add( webView );*/

        // Create a button for Home
        Button homeBtn = new Button( "\uD83C\uDFE0 Home" );
        homeBtn.setMnemonicParsing( false );
/*        homeBtn.setOnAction( event -> {
            webEngine.load( mainPage );
        } );*/

        // Create a toolbar to add the Home button
        ToolBar toolBar = new ToolBar( homeBtn);
        VBox.setVgrow( toolBar, Priority.NEVER );
        // Add MenuBar and AnchorPane to root VBox
        root.getChildren( ).addAll( toolBar, anchorPane );

        return root;
    }

    public static void configureStage( Stage primaryStage, String mainPage, ConfigurableApplicationContext springContext ) {

        Scene scene = new Scene( createMainView(mainPage), 600, 400 );
        // Configure Stage
        primaryStage.setTitle( "Desktop Web Application" );
        primaryStage.setScene( scene );
        primaryStage.setResizable( true );

        // Handle close event
        primaryStage.setOnCloseRequest( event -> {
            Platform.exit( );
            if ( springContext != null ) {
                springContext.close( );
            }
            System.exit( 0 );
        } );
    }

}
