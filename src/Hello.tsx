import React, { useEffect, useState } from "react";
import { Alert, Button, Text, View } from "react-native";
import C72RfidScanner from "../C72RfidScanner";

export default function App() {
  const [isReading, setIsReading] = useState<boolean>(false);
  const [powerState, setPowerState] = useState<string>("");
  const [tags, setTags] = useState<string[]>([]);

  const powerListener = (args: any[]): void => {
    const data = args[0] as string;
    setPowerState(data);
  };

  const tagListener = (args: any[]) => {
    const [epc] = args; // lấy EPC từ mảng
    setTags((prevTags) => [...prevTags, epc]);
  };

  useEffect(() => {
    C72RfidScanner.powerListener?.(powerListener);
    C72RfidScanner.tagListener?.(tagListener);

    return () => {
      C72RfidScanner.deInitializeReader?.();
    };
  }, []);

  const startSystem = async () => {
    C72RfidScanner.initializeReader?.();
  };
  const stopSystem = async () => {
    C72RfidScanner.deInitializeReader?.();
  };

  const readPower = async () => {
    try {
      const result = await C72RfidScanner.readPower();
      console.log(`The result is ${result}`);
    } catch (error: any) {
      console.log(error.message || "Unknown error");
    }
  };

  const scanSingleTag = async () => {
    try {
      const result = await C72RfidScanner.readSingleTag();
      console.log(`The result is ${result}`);
      console.log(result);
    } catch (error: any) {
      console.log(error.message || "Unknown error");
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
        <Button onPress={startSystem} title="Start Systems" />
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
