import React, { useEffect, useState } from "react";
import { Alert, Button, Text, View } from "react-native";

export default function App() {
  const [isReading, setIsReading] = useState<boolean>(false);
  const [powerState, setPowerState] = useState<string>("");
  const [tags, setTags] = useState<string[]>([]);

  const startSystem = async () => {};
  const stopSystem = async () => {};
  const readPower = async () => {};
  const scanSingleTag = async () => {};
  const startReading = async () => {};
  const stopReading = async () => {};

  return (
    <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
      <View>
        <Text>{powerState}</Text>
      </View>

      <View style={{ marginVertical: 20 }}>
        <Button onPress={startSystem} title="Start System" />
      </View>

      <View style={{ marginVertical: 20 }}>
        <Button onPress={stopSystem} title="Stop System" />
      </View>

      <View style={{ marginVertical: 20 }}>
        <Button onPress={readPower} title="Read Power" />
      </View>

      <View style={{ marginVertical: 20 }}>
        <Button onPress={scanSingleTag} title="Read Single Tag" />
      </View>

      <View style={{ marginVertical: 20 }}>
        <Button
          disabled={isReading}
          onPress={startReading}
          title="Start Bulk Scan"
        />
      </View>

      <View style={{ marginVertical: 20 }}>
        <Button
          disabled={!isReading}
          onPress={stopReading}
          title="Stop Bulk Scan"
        />
      </View>
    </View>
  );
}
