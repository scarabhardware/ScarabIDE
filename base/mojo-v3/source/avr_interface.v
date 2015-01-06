module avr_interface(
		input clk,
		input rst,
		input cclk,
		
		output spi_miso,
		input spi_mosi,
		input spi_sck,
		input spi_ss,
		output [3:0] spi_channel,
		
		output tx,
		input rx,
		
		input [3:0] channel,
		output new_sample,
		output [9:0] sample,
		output [3:0] sample_channel,
		
		input [7:0] tx_data,
		input new_tx_data,
		output tx_busy,
		input tx_block,
		
		output [7:0] rx_data,
		output new_rx_data
	);
	
	wire ready;
	wire n_rdy = !ready;
	wire spi_done;
	wire [7:0] spi_dout;
	
	wire tx_m;
	wire spi_miso_m;
	
	reg byte_ct_d, byte_ct_q;
	reg [9:0] sample_d, sample_q;
	reg new_sample_d, new_sample_q;
	reg [3:0] sample_channel_d, sample_channel_q;
	
	cclk_detector cclk_detector (
		.clk(clk),
		.rst(rst),
		.cclk(cclk),
		.ready(ready)
	);
	
	spi_slave spi_slave (
		.clk(clk),
		.rst(n_rdy),
		.ss(spi_ss),
		.mosi(spi_mosi),
		.miso(spi_miso_m),
		.sck(spi_sck),
		.done(spi_done),
		.din(8'hff),
		.dout(spi_dout)
	);
	
	serial_rx #(.CLK_PER_BIT(100), .CTR_SIZE(7)) serial_rx (
		.clk(clk),
		.rst(n_rdy),
		.rx(rx),
		.data(rx_data),
		.new_data(new_rx_data)
	);
	
	serial_tx #(.CLK_PER_BIT(100), .CTR_SIZE(7)) serial_tx (
		.clk(clk),
		.rst(n_rdy),
		.tx(tx_m),
		.block(tx_block),
		.busy(tx_busy),
		.data(tx_data),
		.new_data(new_tx_data)
	);
	
	assign new_sample = new_sample_q;
	assign sample = sample_q;
	assign sample_channel = sample_channel_q;
	
	assign spi_channel = ready ? channel : 4'bZZZZ;
	assign spi_miso = ready && !spi_ss ? spi_miso_m : 1'bZ;
	assign tx = ready ? tx_m : 1'bZ;
	
	always @(*) begin
		byte_ct_d = byte_ct_q;
		sample_d = sample_q;
		new_sample_d = 1'b0;
		sample_channel_d = sample_channel_q;
		
		if (spi_ss) begin
			byte_ct_d = 1'b0;
		end
		
		if (spi_done) begin
			if (byte_ct_q == 1'b0) begin
				sample_d[7:0] = spi_dout;
				byte_ct_d = 1'b1;
			end else begin
				sample_d[9:8] = spi_dout[1:0];
				sample_channel_d = spi_dout[7:4];
				byte_ct_d = 1'b1;
				new_sample_d = 1'b1;
			end
		end
	end
	
	always @(posedge clk) begin
		if (n_rdy) begin
			byte_ct_q <= 1'b0;
			sample_q <= 10'b0;
			new_sample_q <= 1'b0;
		end else begin
			byte_ct_q <= byte_ct_d;
			sample_q <= sample_d;
			new_sample_q <= new_sample_d;
		end
		
		sample_channel_q <= sample_channel_d;
	end
	
endmodule