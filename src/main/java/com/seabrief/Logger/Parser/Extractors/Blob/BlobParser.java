package com.seabrief.Logger.Parser.Extractors.Blob;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import com.seabrief.Logger.Models.CDPDataType;
import com.seabrief.Logger.Models.Metadata;

public class BlobParser {

    public static HashMap<Integer, Double> decodeCompactBlob(byte[] bytes, HashMap<Integer, Metadata> metadata) {
        HashMap<Integer, Double> values = new HashMap<>();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.getShort();

        while (buffer.remaining() > 0) {
            short id_info = buffer.getShort();
            int id = id_info & 0x7FFF;

            CDPDataType type = CDPDataType.valueOf(metadata.get(id).getType().toUpperCase());

            byte[] valueBytes = new byte[getValueSize(type)];
            buffer.get(valueBytes);

            double value = decodeValue(type, valueBytes);

            values.put(id, value);
        }

        return values;
    }

    public static HashMap<Integer, Double> decodeSplitBlob(byte[] bytes) {
        HashMap<Integer, Double> values = new HashMap<>();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.getShort();

        while (buffer.remaining() > 0) {
            short id_info = buffer.getShort();
            int id = id_info & 0x7FFF;

            CDPDataType type = CDPDataType.values()[buffer.get()];

            byte[] valueBytes = new byte[getValueSize(type)];
            buffer.get(valueBytes);

            double value = decodeValue(type, valueBytes);

            values.put(id, value);
        }

        return values;
    }

    private static int getValueSize(CDPDataType dataType) {
        switch (dataType) {
            case DOUBLE:
                return 8;
            case UINT64:
            case INT64:
                return 8;
            case FLOAT:
                return 4;
            case UINT:
            case INT:
                return 4;
            case USHORT:
            case SHORT:
                return 2;
            case UCHAR:
            case CHAR:
                return 1;
            case BOOL:
                return 1;
            case UNDEFINED:
                return 0;
            default:
                throw new IllegalArgumentException("Unknown data type: " + dataType);
        }
    }

    private static double decodeValue(CDPDataType dataType, byte[] valueBytes) {
        ByteBuffer buffer = ByteBuffer.wrap(valueBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        switch (dataType) {
            case DOUBLE:
                return buffer.getDouble();
            case UINT64:
                return buffer.getLong() & 0xFFFFFFFFFFFFFFFFL;
            case INT64:
                return buffer.getLong();
            case FLOAT:
                return buffer.getFloat();
            case UINT:
                return buffer.getInt() & 0xFFFFFFFFL;
            case INT:
                return buffer.getInt();
            case USHORT:
                return buffer.getShort() & 0xFFFF;
            case SHORT:
                return buffer.getShort();
            case UCHAR:
                return buffer.get() & 0xFF;
            case CHAR:
                return buffer.getChar();
            case BOOL:
                return (buffer.get() != 0) ? 1 : 0;
            case UNDEFINED:
                return 0;
            default:
                throw new IllegalArgumentException("Unknown data type: " + dataType);
        }
    }
}
