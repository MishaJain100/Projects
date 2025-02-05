import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Consumer;

import java.util.UUID;
import javafx.scene.paint.Color;

public class App extends Application 
{
    private final Map<String, User> users = new HashMap<>();
    private User currentUser;
    private Stage primaryStage;
    private Scene mainScene;
    private StackPane rootStack;
    private Node blurredBackground;

    private void register(String username, String password, String confirmPassword) 
    {
        if (username.isEmpty() || password.isEmpty()) 
        {
            showAlert("Registration Error", "Please fill in all fields.");
            return;
        }
        if (!password.equals(confirmPassword)) 
        {
            showAlert("Registration Error", "Passwords do not match.");
            return;
        }
        if (users.containsKey(username)) 
        {
            showAlert("Registration Error", "Username already exists.");
            return;
        }
        User newUser = new User(username, password);
        users.put(username, newUser);
        currentUser = newUser;
        showRoomSetupScreen();
    }
    
    private void showRoomSetupScreen() 
    {
        VBox setupScreen = new VBox(20);
        setupScreen.setAlignment(Pos.CENTER);
        setupScreen.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a237e, #0d47a1);");
        setupScreen.setPadding(new Insets(40));
        Label titleLabel = new Label("Setup Your Home");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");
        RoomSelectionForm roomForm = new RoomSelectionForm(room -> {
            currentUser.addRoom(room);
            showMainScreen();
        });
        setupScreen.getChildren().addAll(titleLabel, roomForm);
        Scene scene = new Scene(setupScreen, 800, 600);
        primaryStage.setScene(scene);
    }
    
    private void updateRoomDisplay(Room room) {
        ScrollPane scrollPane = (ScrollPane) rootStack.lookup("#roomScrollPane");
        if (scrollPane != null) {
            TilePane roomGrid = (TilePane) scrollPane.getContent();
            
            for (Node node : roomGrid.getChildren()) {
                if (node instanceof StackPane && roomMatchesNode((StackPane) node, room)) {
                    VBox roomInfo = (VBox) ((StackPane) node).getChildren().get(1);
    
                    Label fanSpeedLabel = (Label) ((VBox) roomInfo.getChildren().get(1)).getChildren().get(0);
                    Label brightnessLabel = (Label) ((VBox) roomInfo.getChildren().get(1)).getChildren().get(1);
                    fanSpeedLabel.setText("Fan: " + room.getFanSpeed());
                    brightnessLabel.setText("Light: " + room.getBrightness() + "%");
    
                    Rectangle background = (Rectangle) ((StackPane) node).getChildren().get(0);
                    Color baseColor = room.getColor();
                    double brightnessFactor = room.getBrightness() / 100.0;
                    Color adjustedColor = baseColor.interpolate(Color.BLACK, 1 - brightnessFactor);
                    background.setFill(adjustedColor);
                    
                    break;
                }
            }
        }
    }
    
    
    private boolean roomMatchesNode(StackPane node, Room room) {
        Label nameLabel = (Label) ((VBox) node.getChildren().get(1)).getChildren().get(0);
        return nameLabel.getText().equals(room.getName());
    }
    
    
    private void showAlert(String title, String content) 
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: white;" +
            "-fx-padding: 20;"
        );
        alert.showAndWait();
    }

    private void login(String username, String password) 
    {
    if (username.isEmpty() || password.isEmpty()) 
    {
        showAlert("Login Error", "Please fill in all fields.");
        return;
    }
    User user = users.get(username);
    if (user == null || !user.getPassword().equals(password)) 
    {
        showAlert("Login Error", "Invalid username or password.");
        return;
    }
    currentUser = user;
    showMainScreen();
}

    private void showMainScreen() 
    {
        rootStack = new StackPane();
        rootStack.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a237e, #0d47a1);");
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER);
        HBox header = createHeader();
        ScrollPane scrollPane = new ScrollPane(createRoomGrid());
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        HBox masterControls = createMasterControls();
        mainContent.getChildren().addAll(header, scrollPane, masterControls);
        rootStack.getChildren().add(mainContent);
        mainScene = new Scene(rootStack, 800, 600);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), rootStack);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        primaryStage.setScene(mainScene);
    }

    private HBox createHeader() 
    {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 10;");
        Label welcomeLabel = new Label("Welcome, " + currentUser.getUsername());
        welcomeLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        Button addRoomButton = createStyledButton("+ Add Room", "#4caf50");
        addRoomButton.setOnAction(e -> showAddRoomDialog());
        Button logoutButton = createStyledButton("Logout", "#f44336");
        logoutButton.setOnAction(e -> logout());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(welcomeLabel, spacer, addRoomButton, logoutButton);
        return header;
    }

    private TilePane createRoomGrid() 
    {
        TilePane roomGrid = new TilePane();
        roomGrid.setPrefColumns(3);
        roomGrid.setHgap(20);
        roomGrid.setVgap(20);
        roomGrid.setPadding(new Insets(20));
        roomGrid.setStyle("-fx-background-color: transparent;");
        for (Room room : currentUser.getRooms()) 
        {
            Node roomTile = createRoomTile(room);
            roomGrid.getChildren().add(roomTile);
        }
        return roomGrid;
    }

    private HBox createMasterControls() 
    {
        HBox masterControls = new HBox(20);
        masterControls.setAlignment(Pos.CENTER);
        masterControls.setPadding(new Insets(20));
        masterControls.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 10;");
        Button masterOffButton = createStyledButton("MASTER OFF", "#f44336");
        Button masterOnButton = createStyledButton("MASTER ON", "#4caf50");
        masterOffButton.setOnAction(e -> {
            for (Room room : currentUser.getRooms()) {
                room.setFanSpeed(0);
                room.setBrightness(0);
                updateRoomDisplay(room);
            }
        });
        masterOnButton.setOnAction(e -> {
            for (Room room : currentUser.getRooms()) {
                room.setFanSpeed(5);
                room.setBrightness(100);
                updateRoomDisplay(room);
            }
        });
        masterControls.getChildren().addAll(masterOffButton, masterOnButton);
        return masterControls;
    }

    private void showAddRoomDialog() {
        Rectangle overlay = new Rectangle(primaryStage.getWidth(), primaryStage.getHeight());
        overlay.setFill(Color.rgb(0, 0, 0, 0.5));
    
        final RoomSelectionForm[] roomForm = new RoomSelectionForm[1];
        
        roomForm[0] = new RoomSelectionForm(room -> {
            currentUser.addRoom(room);
    
            Node roomTile = createRoomTile(room);
            TilePane roomGrid = (TilePane) ((ScrollPane) rootStack.getChildren().get(0).lookup(".scroll-pane")).getContent();
            roomGrid.getChildren().add(roomTile);
    
            ScaleTransition st = new ScaleTransition(Duration.millis(300), roomTile);
            st.setFromX(0);
            st.setFromY(0);
            st.setToX(1);
            st.setToY(1);
            st.play();
    
            rootStack.getChildren().removeAll(overlay, roomForm[0]);
        });
    
        Button cancelButton = roomForm[0].getCancelButton();
        cancelButton.setOnAction(e -> {
            rootStack.getChildren().removeAll(overlay, roomForm[0]);
        });
    
        rootStack.getChildren().addAll(overlay, roomForm[0]);
    
        roomForm[0].setScaleX(0.7);
        roomForm[0].setScaleY(0.7);
    
        ScaleTransition st = new ScaleTransition(Duration.millis(300), roomForm[0]);
        st.setToX(1);
        st.setToY(1);
        st.play();
    }    

    private void logout() 
    {
        currentUser = null;
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), rootStack);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> showInitialScreen());
        fadeOut.play();
    }

    @Override
    public void start(Stage primaryStage) 
    {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Smart Home Automation");
        showInitialScreen();
        primaryStage.show();
    }

    public static void main(String[] args) 
    {
        launch(args);
    }

    private void showInitialScreen() 
    {
        VBox initialScreen = new VBox(20);
        initialScreen.setAlignment(Pos.CENTER);
        initialScreen.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a237e, #0d47a1);");
        initialScreen.setPadding(new Insets(40));
        Label titleLabel = new Label("Smart Home Automation");
        titleLabel.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: white;");
        Button loginButton = createStyledButton("Login", "#2196f3");
        Button registerButton = createStyledButton("Register", "#4caf50");
        loginButton.setOnAction(e -> showLoginScreen());
        registerButton.setOnAction(e -> showRegisterScreen());
        addButtonHoverEffect(loginButton);
        addButtonHoverEffect(registerButton);
        initialScreen.getChildren().addAll(titleLabel, loginButton, registerButton);
        Scene scene = new Scene(initialScreen, 800, 600);
        primaryStage.setScene(scene);
    }

    private void showLoginScreen() 
    {
        VBox loginScreen = new VBox(15);
        loginScreen.setAlignment(Pos.CENTER);
        loginScreen.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a237e, #0d47a1);");
        loginScreen.setPadding(new Insets(40));
        loginScreen.setMaxWidth(400);
        Label titleLabel = new Label("Login");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");
        TextField usernameField = createStyledTextField("Username");
        PasswordField passwordField = createStyledPasswordField("Password");
        Button loginButton = createStyledButton("Login", "#2196f3");
        Button backButton = createStyledButton("Back", "#9e9e9e");
        loginButton.setOnAction(e -> login(usernameField.getText(), passwordField.getText()));
        backButton.setOnAction(e -> showInitialScreen());
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton, backButton);
        formContainer.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-padding: 30; " +
                             "-fx-background-radius: 10;");
        loginScreen.getChildren().add(formContainer);
        Scene scene = new Scene(loginScreen, 800, 600);
        primaryStage.setScene(scene);
    }

    private void showRegisterScreen() 
    {
        VBox registerScreen = new VBox(15);
        registerScreen.setAlignment(Pos.CENTER);
        registerScreen.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a237e, #0d47a1);");
        registerScreen.setPadding(new Insets(40));
        registerScreen.setMaxWidth(400);
        Label titleLabel = new Label("Register");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");
        TextField usernameField = createStyledTextField("Username");
        PasswordField passwordField = createStyledPasswordField("Password");
        PasswordField confirmPasswordField = createStyledPasswordField("Confirm Password");
        Button registerButton = createStyledButton("Register", "#4caf50");
        Button backButton = createStyledButton("Back", "#9e9e9e");
        registerButton.setOnAction(e -> register(usernameField.getText(), passwordField.getText(), 
                                               confirmPasswordField.getText()));
        backButton.setOnAction(e -> showInitialScreen());
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.getChildren().addAll(titleLabel, usernameField, passwordField, 
                                         confirmPasswordField, registerButton, backButton);
        formContainer.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-padding: 30; " +
                             "-fx-background-radius: 10;");
        registerScreen.getChildren().add(formContainer);
        Scene scene = new Scene(registerScreen, 800, 600);
        primaryStage.setScene(scene);
    }

    private Node createRoomTile(Room room) 
    {
        StackPane roomTile = new StackPane();
        roomTile.setPrefSize(200, 200);
        Rectangle background = new Rectangle(200, 200);
        background.setArcWidth(20);
        background.setArcHeight(20);
        background.setFill(Color.valueOf("#2196f3"));
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10);
        dropShadow.setSpread(0.2);
        background.setEffect(dropShadow);
        VBox roomInfo = new VBox(10);
        roomInfo.setAlignment(Pos.CENTER);
        Label nameLabel = new Label(room.getName());
        nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        VBox statsBox = new VBox(5);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setOpacity(0);
        Label fanSpeedLabel = new Label("Fan: " + room.getFanSpeed());
        Label brightnessLabel = new Label("Light: " + room.getBrightness() + "%");
        statsBox.getChildren().addAll(fanSpeedLabel, brightnessLabel);
        roomInfo.getChildren().addAll(nameLabel, statsBox);
        roomTile.getChildren().addAll(background, roomInfo);
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), roomTile);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), statsBox);
        roomTile.setOnMouseEntered(e -> {
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
            scaleTransition.play();
            fadeTransition.setToValue(1);
            fadeTransition.play();
        });
        roomTile.setOnMouseExited(e -> {
            scaleTransition.setToX(1);
            scaleTransition.setToY(1);
            scaleTransition.play();
            fadeTransition.setToValue(0);
            fadeTransition.play();
        });
        roomTile.setOnMouseClicked(e -> showRoomControls(room));
        return roomTile;
    }

    private void showRoomControls(Room room) {
        if (blurredBackground == null)
            blurredBackground = rootStack.getChildren().get(0);
    
        GaussianBlur blur = new GaussianBlur(0);
        blurredBackground.setEffect(blur);
    
        Timeline blurTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 10))
        );
        blurTimeline.play();
    
        VBox controlPanel = new VBox(20);
        controlPanel.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        controlPanel.setPadding(new Insets(20));
        controlPanel.setMaxWidth(600);
        controlPanel.setMaxHeight(400);
    
        Label fanLabel = new Label("Fan Speed");
        fanLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        
        // Fan Toggle Switch
        ToggleButton fanToggle = new ToggleButton("Fan OFF");
        fanToggle.setStyle("-fx-font-size: 14;");
        fanToggle.setSelected(room.getFanSpeed() > 0);  // Set initial state based on room fan speed
        fanToggle.setOnAction(e -> {
            if (fanToggle.isSelected()) {
                fanToggle.setText("Fan ON");
                room.setFanSpeed(1);  // Set default fan speed to 1 when turning on
            } else {
                fanToggle.setText("Fan OFF");
                room.setFanSpeed(0);  // Set fan speed to 0 when turning off
            }
            updateRoomDisplay(room);
        });
    
        HBox fanSpeedControl = createFanSpeedControl(room);
        fanSpeedControl.disableProperty().bind(fanToggle.selectedProperty().not());  // Disable fan control when off
    
        Label lightLabel = new Label("Light Brightness");
        lightLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
    
        // Light Toggle Switch
        ToggleButton lightToggle = new ToggleButton("Light OFF");
        lightToggle.setStyle("-fx-font-size: 14;");
        lightToggle.setSelected(room.getBrightness() > 0);  // Set initial state based on room brightness
        lightToggle.setOnAction(e -> {
            if (lightToggle.isSelected()) {
                lightToggle.setText("Light ON");
                room.setBrightness(50);  // Set default brightness to 50% when turning on
            } else {
                lightToggle.setText("Light OFF");
                room.setBrightness(0);  // Set brightness to 0 when turning off
            }
            updateRoomDisplay(room);
        });
    
        Slider brightnessSlider = new Slider(0, 100, room.getBrightness());
        brightnessSlider.setShowTickLabels(true);
        brightnessSlider.setShowTickMarks(true);
        brightnessSlider.disableProperty().bind(lightToggle.selectedProperty().not());  // Disable brightness slider when off
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (lightToggle.isSelected()) {
                room.setBrightness(newVal.doubleValue());
                updateRoomDisplay(room);
            }
        });
    
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button offButton = createStyledButton("OFF", "#f44336");
        Button saveButton = createStyledButton("Save", "#4caf50");
    
        offButton.setOnAction(e -> {
            room.setFanSpeed(0);
            room.setBrightness(0);
            fanToggle.setSelected(false);
            lightToggle.setSelected(false);
            updateRoomDisplay(room);
        });
    
        saveButton.setOnAction(e -> closeRoomControls(blur));
    
        buttonBox.getChildren().addAll(offButton, saveButton);
    
        controlPanel.getChildren().addAll(
            fanLabel, fanToggle, fanSpeedControl,
            lightLabel, lightToggle, brightnessSlider,
            buttonBox
        );
    
        rootStack.getChildren().add(controlPanel);
    
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), controlPanel);
        scaleIn.setFromX(0.7);
        scaleIn.setFromY(0.7);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.play();
    }
    


    private HBox createFanSpeedControl(Room room) {
        HBox fanControl = new HBox(10);
        fanControl.setAlignment(Pos.CENTER);
        ToggleGroup fanSpeedGroup = new ToggleGroup();
    
        for (int i = 0; i <= 5; i++) {
            ToggleButton speedButton = new ToggleButton(String.valueOf(i));
            speedButton.setToggleGroup(fanSpeedGroup);
            speedButton.setUserData(i);
            
            speedButton.setStyle(
                "-fx-background-radius: 20; -fx-min-width: 40; -fx-min-height: 40; " +
                "-fx-background-color: " + (i == room.getFanSpeed() ? "#2196f3" : "#e0e0e0") + ";"
            );
    
            fanControl.getChildren().add(speedButton);
        }
    
        fanSpeedGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int selectedSpeed = (Integer) newVal.getUserData();
                room.setFanSpeed(selectedSpeed);
                updateRoomDisplay(room);
    
                for (Node node : fanControl.getChildren()) {
                    ToggleButton button = (ToggleButton) node;
                    int speed = (Integer) button.getUserData();
                    if (speed == selectedSpeed) {
                        button.setStyle(
                            "-fx-background-radius: 20; -fx-min-width: 40; -fx-min-height: 40; " +
                            "-fx-background-color: #2196f3;"
                        );
                    } else {
                        button.setStyle(
                            "-fx-background-radius: 20; -fx-min-width: 40; -fx-min-height: 40; " +
                            "-fx-background-color: #e0e0e0;"
                        );
                    }
                }
            }
        });
    
        return fanControl;
    }
    

    private void closeRoomControls(GaussianBlur blur) 
    {
        Timeline blurTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 10)),
            new KeyFrame(Duration.millis(300), new KeyValue(blur.radiusProperty(), 0))
        );
        blurTimeline.play();
        Node controlPanel = rootStack.getChildren().get(rootStack.getChildren().size() - 1);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(300), controlPanel);
        scaleOut.setToX(0.7);
        scaleOut.setToY(0.7);
        scaleOut.setOnFinished(e -> rootStack.getChildren().remove(controlPanel));
        scaleOut.play();
    }

    private Button createStyledButton(String text, String color) 
    {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;"
        );
        return button;
    }

    private TextField createStyledTextField(String prompt) 
    {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.2); " +
            "-fx-text-fill: white; " +
            "-fx-prompt-text-fill: rgba(255, 255, 255, 0.7); " +
            "-fx-padding: 10; " +
            "-fx-background-radius: 5;"
        );
        return textField;
    }

    private PasswordField createStyledPasswordField(String prompt) 
    {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(prompt);
        passwordField.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.2); " +
            "-fx-text-fill: white; " +
            "-fx-prompt-text-fill: rgba(255, 255, 255, 0.7); " +
            "-fx-padding: 10; " +
            "-fx-background-radius: 5;"
        );
        return passwordField;
    }

    private void addButtonHoverEffect(Button button) 
    {
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });
    }
}

class User 
{
    private final String username;
    private final String password;
    private final Map<String, Room> rooms = new HashMap<>();
    
    public User(String username, String password) 
    {
        this.username = username;
        this.password = password;
    }

    public String getPassword() 
    {
        return password;
    }

    public String getUsername() 
    {
        return username;
    }

    public void addRoom(Room room) 
    {
        rooms.put(room.getName(), room);
    }

    public Collection<Room> getRooms() 
    {
        return rooms.values();
    }

    public Room getRoom(String name) 
    {
        return rooms.get(name);
    }

    public void removeRoom(String name) 
    {
        rooms.remove(name);
    }
}

class Room {
    private String id;
    private String name;
    private int fanSpeed;
    private double brightness;
    private final String type;
    private final Color color;

    public Room(String name, String type) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.fanSpeed = 0;
        this.brightness = 0;
        this.color = getRoomTypeColor(type);
    }

    private Color getRoomTypeColor(String type) {
        return switch (type.toLowerCase()) {
            case "bedroom" -> Color.valueOf("#2196f3");
            case "kitchen" -> Color.valueOf("#4caf50");
            case "bathroom" -> Color.valueOf("#9c27b0");
            case "living room" -> Color.valueOf("#ff9800");
            default -> Color.valueOf("#757575");
        };
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(int fanSpeed) {
        this.fanSpeed = Math.min(Math.max(fanSpeed, 0), 5);
    }

    public double getBrightness() {
        return brightness;
    }

    public void setBrightness(double brightness) {
        this.brightness = Math.min(Math.max(brightness, 0), 100);
    }

    public String getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }
}

class RoomSelectionForm extends VBox 
{
    private final ComboBox<String> roomTypeCombo;
    private final TextField roomNameField;
    private final Button addButton;
    private final Button cancelButton;

    public RoomSelectionForm(Consumer<Room> onRoomAdded) 
    {
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        setMaxWidth(400);
        Label titleLabel = new Label("Add New Room");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        roomTypeCombo = new ComboBox<>();
        roomTypeCombo.getItems().addAll("Bedroom", "Kitchen", "Bathroom", "Living Room");
        roomTypeCombo.setPromptText("Select Room Type");
        roomTypeCombo.setMaxWidth(Double.MAX_VALUE);
        roomNameField = new TextField();
        roomNameField.setPromptText("Room Name");
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        addButton = new Button("Add Room");
        addButton.setStyle(
            "-fx-background-color: #4caf50; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;"
        );
        cancelButton = new Button("Cancel");
        cancelButton.setStyle(
            "-fx-background-color: #f44336; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;"
        );
        addButton.setOnAction(e -> {
            if (roomTypeCombo.getValue() != null && !roomNameField.getText().isEmpty()) {
                Room newRoom = new Room(roomNameField.getText(), roomTypeCombo.getValue());
                onRoomAdded.accept(newRoom);
            }
        });
        buttonBox.getChildren().addAll(addButton, cancelButton);
        getChildren().addAll(titleLabel, roomTypeCombo, roomNameField, buttonBox);
    }

    public Button getCancelButton() 
    {
        return cancelButton;
    }
}

class FanSpeedIndicator extends Group 
{
    private final Circle circle;
    private final Group blades;
    private final RotateTransition rotation;

    public FanSpeedIndicator(double size) 
    {
        circle = new Circle(size / 2, Color.LIGHTGRAY);
        circle.setStroke(Color.GRAY);
        circle.setStrokeWidth(2);
        blades = new Group();
        for (int i = 0; i < 4; i++) 
        {
            Rectangle blade = new Rectangle(-5, -size / 3, 10, size / 2);
            blade.setFill(Color.GRAY);
            blade.setRotate(i * 90);
            blades.getChildren().add(blade);
        }
        blades.setTranslateX(size / 2);
        blades.setTranslateY(size / 2);
        rotation = new RotateTransition(Duration.seconds(2), blades);
        rotation.setByAngle(360);
        rotation.setCycleCount(Timeline.INDEFINITE);
        rotation.setInterpolator(Interpolator.LINEAR);
        getChildren().addAll(circle, blades);
    }

    public void setSpeed(int speed) 
    {
        if (speed == 0)
            rotation.stop();
        else 
        {
            rotation.setRate(speed * 0.5);
            if (!rotation.getStatus().equals(Animation.Status.RUNNING))
                rotation.play();
        }
    }
}