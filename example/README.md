Title screen:                      | Main screen:
:-----------------------------:|:-----------------------------:
![](images/title.jpg)          | ![](images/main.jpg)

## 🚀 How to Run

1. **Setup Dependencies**:
   ```bash
   ./gradlew build
   ```

2. **Run the App**:
   ```bash
   # Android
   ./gradlew composeApp:installDebug
   
   # iOS Simulator
   ./gradlew composeApp:iosSimulatorArm64Test
   ```