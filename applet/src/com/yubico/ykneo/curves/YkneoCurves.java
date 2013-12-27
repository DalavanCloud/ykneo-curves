package com.yubico.ykneo.curves;

/* Copyright (c) 2013 Yubico AB 
 * All rights reserved.
 */

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.security.ECPublicKey;
import javacard.security.KeyPair;
import javacard.security.Signature;

public class YkneoCurves extends Applet {
	public static final short _0 = 0;

	KeyPair brainpool256r1;
	KeyPair secp256r1;
	Signature signature;

	public YkneoCurves() {
		brainpool256r1 = BrainpoolP256r1.newKeyPair();
		secp256r1 = SecP256r1.newKeyPair();
		signature = Signature.getInstance(Signature.ALG_ECDSA_SHA, false);
	}

	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new YkneoCurves().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu) throws ISOException {
		if(selectingApplet()) {
			return;
		}

		short sendlen = 0;
		short recvlen = apdu.setIncomingAndReceive();
		byte[] buf = apdu.getBuffer();
		byte ins = buf[ISO7816.OFFSET_INS];

		switch(ins) {
		case 0x01: {
			brainpool256r1.genKeyPair();
			ECPublicKey pubKey = (ECPublicKey) brainpool256r1.getPublic();
			sendlen = pubKey.getW(buf, _0);
			break;
		}
		case 0x02: {
			signature.init(brainpool256r1.getPrivate(), Signature.MODE_SIGN);
			sendlen = signature.sign(buf, ISO7816.OFFSET_CDATA, recvlen, buf, (short) 0);
			break;
		}
		case 0x11: {
			secp256r1.genKeyPair();
			ECPublicKey pubKey = (ECPublicKey) secp256r1.getPublic();
			sendlen = pubKey.getW(buf, _0);
			break;
		}
		case 0x12: {
			signature.init(secp256r1.getPrivate(), Signature.MODE_SIGN);
			sendlen = signature.sign(buf, ISO7816.OFFSET_CDATA, recvlen, buf, (short) 0);
			break;
		}
		}
		if(sendlen > 0) {
			apdu.setOutgoingAndSend(_0, sendlen);
		}
	}
}
