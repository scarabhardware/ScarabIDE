
module HDMI_Encoder (	
	input inclk,  
	input [7:0] ired,igreen,iblue,  
	input ihSync,ivSync,iDE,  
	output [2:0] OTMDSp, OTMDSn,
	output OTMDSp_clock, OTMDSn_clock	
);

wire [9:0] TMDS_red, TMDS_green, TMDS_blue;
TMDS_encoder encode_R(.clk(inclk), .VD(ired  ), .CD(2'b00)        , .VDE(iDE), .TMDS(TMDS_red));
TMDS_encoder encode_G(.clk(inclk), .VD(igreen), .CD(2'b00)        , .VDE(iDE), .TMDS(TMDS_green));
TMDS_encoder encode_B(.clk(inclk), .VD(iblue ), .CD({ivSync,ihSync}), .VDE(iDE), .TMDS(TMDS_blue));

wire clk_TMDS, DCM_TMDS_CLKFX;   
DCM_SP #(.CLKFX_MULTIPLY(10)) DCM_TMDS_inst(.CLKIN(inclk), .CLKFX(DCM_TMDS_CLKFX), .RST(1'b0));
BUFG BUFG_TMDSp(.I(DCM_TMDS_CLKFX), .O(clk_TMDS));


reg [3:0] TMDS_mod10=0;  
reg [9:0] TMDS_shift_red=0, TMDS_shift_green=0, TMDS_shift_blue=0;
reg TMDS_shift_load=0;
always @(posedge clk_TMDS) TMDS_shift_load <= (TMDS_mod10==4'd9);

always @(posedge clk_TMDS)
begin
	TMDS_shift_red   <= TMDS_shift_load ? TMDS_red   : TMDS_shift_red  [9:1];
	TMDS_shift_green <= TMDS_shift_load ? TMDS_green : TMDS_shift_green[9:1];
	TMDS_shift_blue  <= TMDS_shift_load ? TMDS_blue  : TMDS_shift_blue [9:1];	
	TMDS_mod10 <= (TMDS_mod10==4'd9) ? 4'd0 : TMDS_mod10+4'd1;
end

OBUFDS OBUFDS_red  (.I(TMDS_shift_red  [0]), .O(OTMDSp[2]), .OB(OTMDSn[2]));
OBUFDS OBUFDS_green(.I(TMDS_shift_green[0]), .O(OTMDSp[1]), .OB(OTMDSn[1]));
OBUFDS OBUFDS_blue (.I(TMDS_shift_blue [0]), .O(OTMDSp[0]), .OB(OTMDSn[0]));
OBUFDS OBUFDS_clock(.I(inclk), .O(OTMDSp_clock), .OB(OTMDSn_clock));
endmodule


module TMDS_encoder(
	input clk,
	input [7:0] VD,  // video data (red, green or blue)
	input [1:0] CD,  // control data
	input VDE,  // video data enable, to choose between CD (when VDE=0) and VD (when VDE=1)
	output reg [9:0] TMDS = 0
);

wire [3:0] Nb1s = VD[0] + VD[1] + VD[2] + VD[3] + VD[4] + VD[5] + VD[6] + VD[7];
wire XNOR = (Nb1s>4'd4) || (Nb1s==4'd4 && VD[0]==1'b0);
wire [8:0] q_m = {~XNOR, q_m[6:0] ^ VD[7:1] ^ {7{XNOR}}, VD[0]};

reg [3:0] balance_acc = 0;
wire [3:0] balance = q_m[0] + q_m[1] + q_m[2] + q_m[3] + q_m[4] + q_m[5] + q_m[6] + q_m[7] - 4'd4;
wire balance_sign_eq = (balance[3] == balance_acc[3]);
wire invert_q_m = (balance==0 || balance_acc==0) ? ~q_m[8] : balance_sign_eq;
wire [3:0] balance_acc_inc = balance - ({q_m[8] ^ ~balance_sign_eq} & ~(balance==0 || balance_acc==0));
wire [3:0] balance_acc_new = invert_q_m ? balance_acc-balance_acc_inc : balance_acc+balance_acc_inc;
wire [9:0] TMDS_data = {invert_q_m, q_m[8], q_m[7:0] ^ {8{invert_q_m}}};
wire [9:0] TMDS_code = CD[1] ? (CD[0] ? 10'b1010101011 : 10'b0101010100) : (CD[0] ? 10'b0010101011 : 10'b1101010100);

always @(posedge clk) TMDS <= VDE ? TMDS_data : TMDS_code;
always @(posedge clk) balance_acc <= VDE ? balance_acc_new : 4'h0;
endmodule

