package org.pamguard.x3.sud;

import java.io.IOException;

import com.google.common.io.LittleEndianDataInputStream;

/**
 * The header of a .sud file.
 */
public class SudHeader {
		
		public int HostCodeVersion; //Uint16
		
		public int HostTime; //Uint32
		
		public byte DeviceType;
		
		public byte DeviceCodeVersion;
		
		public int DeviceTime; //Uint32
		
		public int DeviceIdentifier; //Uint32
		
		public int BlockLength; //Uint32
		
		public int StartBlock; //Uint16
		
		public int EndBlock; //Uint16
		
		public int NoOfBlocks; //Uint32
		
		public int Crc; //Uint16
		
		
		public static SudHeader deSerialise(LittleEndianDataInputStream dataInputStream) throws IOException {
			
			SudHeader header = new SudHeader(); 
			
			header.HostCodeVersion = dataInputStream.readUnsignedShort();
			header.HostTime = dataInputStream.readInt();
			header.DeviceType = dataInputStream.readByte();
			header.DeviceCodeVersion = dataInputStream.readByte(); 
			header.DeviceTime = dataInputStream.readInt();
			header.DeviceIdentifier = dataInputStream.readInt();
			header.BlockLength = dataInputStream.readInt();
			header.StartBlock = dataInputStream.readUnsignedShort();
			header.EndBlock = dataInputStream.readUnsignedShort();
			header.NoOfBlocks = dataInputStream.readInt();
			header.Crc = dataInputStream.readUnsignedShort();
			

			
			return header;

		}
		
		
		public String toHeaderString() {
			return String.format("HostCodeVersion: %d\nHostTime: %d\nDeviceType: %d\nDeviceCodeVersion: %d\nDeviceTime: %d\nDeviceIdentifier: %d\nBlockLength: %d\n"
					+ "StartBlock: %d\nEndBlock: %d\nNoOfBlocks: %d\nCrc: %d\n", 
					HostCodeVersion, HostTime, DeviceType, DeviceCodeVersion, DeviceTime, DeviceIdentifier, BlockLength, 
					StartBlock, EndBlock, NoOfBlocks, Crc);
			
			
		}


}
