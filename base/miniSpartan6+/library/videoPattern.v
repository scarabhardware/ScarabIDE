module videoGenerator(
	input inclk,  
	output reg [7:0] ored,ogreen,oblue,  
	output reg ohSync,ovSync,oDE  
    );
	 
reg [9:0] CounterX=0;
reg [9:0] CounterY=0;

always @(posedge inclk) oDE <= (CounterX<640) && (CounterY<480);

always @(posedge inclk) CounterX <= (CounterX==799) ? 0 : CounterX+1;
always @(posedge inclk) if(CounterX==799) CounterY <= (CounterY==524) ? 0 : CounterY+1;

always @(posedge inclk) ohSync <= (CounterX>=656) && (CounterX<752);
always @(posedge inclk) ovSync <= (CounterY>=490) && (CounterY<492);

wire [7:0] W = {8{CounterX[7:0]==CounterY[7:0]}};
wire [7:0] A = {8{CounterX[7:5]==3'h2 && CounterY[7:5]==3'h2}};

always @(posedge inclk) ored <= (CounterX<160)||(CounterX>480) ? 255: 0;
always @(posedge inclk) ogreen <= ((CounterX>160)&&(CounterX<320))||(CounterX>480) ? 255 : 0;
always @(posedge inclk) oblue <= (CounterX>320) ? 255 : 0;
	 


endmodule
