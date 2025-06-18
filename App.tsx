import { StatusBar } from "expo-status-bar";
import { StyleSheet, Text, View } from "react-native";
import { NativeModules } from "react-native";

const { CalendarModule, C72RfidScanner } = NativeModules;

export default function App() {
  console.log("Native Modules 22:", NativeModules);
    console.log("CalendarModule: ", CalendarModule)
  console.log("C72RfidScanner2: ", C72RfidScanner.initializeReader)

   CalendarModule.createCalendarEvent("hello", "good2!");


  return (
    <View style={styles.container}>
      <Text>Open up App.tsx to start working on your app!</Text>
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
    alignItems: "center",
    justifyContent: "center",
  },
});
