import React, { useEffect, useState } from "react";
import { Alert, Button, Text, View } from "react-native";
import C72RfidScanner from "../C72RfidScanner";

const App = () => {
  const [isReading, setIsReading] = useState<boolean>(false);
  const [powerState, setPowerState] = useState<string>("");
  const [tags, setTags] = useState<string[]>([]);

  const showAlert = (title: string, data: string) => {
    Alert.alert(
      title,
      data,
      [{ text: "OK", onPress: () => console.log("OK Pressed") }],
      {
        cancelable: false,
      }
    );
  };

  const powerListener = (args: any[]): void => {
    const data = args[0] as string;
    setPowerState(data);
  };

  const tagListener = (args: any[]) => {
    const [epc] = args; // lấy EPC từ mảng
    setTags((prevTags) => [...prevTags, epc]);
  };

  useEffect(() => {
    const scanner = C72RfidScanner;
    scanner.initializeReader?.();
    scanner.powerListener?.(powerListener);
    scanner.tagListener?.(tagListener);

    return () => {
      scanner.deInitializeReader?.();
    };
  }, []);

  const readPower = async () => {
    try {
      const result = await C72RfidScanner.readPower();
      showAlert("SUCCESS", `The result is ${result}`);
      console.log(result);
    } catch (error: any) {
      showAlert("FAILED", error.message || "Unknown error");
    }
  };

  const scanSingleTag = async () => {
    try {
      const result = await C72RfidScanner.readSingleTag();
      showAlert("SUCCESS", `The result is ${result}`);
      console.log(result);
    } catch (error: any) {
      showAlert("FAILED", error.message || "Unknown error");
    }
  };

  const startReading = () => {
    C72RfidScanner.startReadingTags?.((args: any[]) => {
      const message = args[0] as boolean;
      setIsReading(message);
    });
  };

  const stopReading = () => {
    C72RfidScanner.stopReadingTags?.((args: any[]) => {
      setIsReading(false); // bạn đang hardcode `false` nên không cần dùng `args` nếu không cần
    });

    console.log("Collected Tags:", tags);
  };

  return (
    <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
      <View>
        <Text>{powerState}</Text>
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
};

export default App;
