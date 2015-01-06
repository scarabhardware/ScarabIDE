module top(
	input pixclk,  
	output [2:0] TMDSp, TMDSn,
	output TMDSp_clock, TMDSn_clock,
	output [7:0] LED
);
videoGenerator  videoGenerator1(
	.inclk(pixclk),
	.ored(red),
	.ogreen(green),
	.oblue(blue),
	.ohSync(hSync),
	.ovSync(vSync),
	.oDE(DrawArea)
);	
HDMI_Encoder HDMI1_Encoder(
	.inclk(pixclk),
	.ired(red),
	.igreen(green),
	.iblue(blue),
	.ihSync(hSync),
	.ivSync(vSync),
	.iDE(DrawArea),
	.OTMDSp(TMDSp),
	.OTMDSn(TMDSn),
	.OTMDSp_clock(TMDSp_clock),
	.OTMDSn_clock(TMDSn_clock)
	);
endmodule
