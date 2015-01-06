module cclk_detector(
		input clk,
		input rst,
		input cclk,
		output ready
	);
	
	reg [8:0] ctr_d, ctr_q;
	reg ready_d, ready_q;
	
	assign ready = ready_q;
	
	always @(ctr_q or cclk) begin
		ready_d = 1'b0;
		if (cclk == 1'b0) begin
			ctr_d = 9'b0;
		end else if (ctr_q != {9{1'b1}}) begin
			ctr_d = ctr_q + 1'b1;
		end else begin
			ctr_d = ctr_q;
			ready_d = 1'b1;
		end
		
	end
	
	always @(posedge clk) begin
		if (rst) begin
			ctr_q <= 9'b0;
			ready_q <= 1'b0;
		end else begin
			ctr_q <= ctr_d;
			ready_q <= ready_d;
		end
	end
	
endmodule