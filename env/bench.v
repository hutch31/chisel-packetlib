module bench;
  reg clock;
  reg reset;
  reg io_sendPacket_valid;
  reg [15:0] io_sendPacket_bits_length;
  reg [15:0] io_sendPacket_bits_pid;
  reg 	     io_sendPacket_bits_packetGood;
  

  /*AUTOWIRE*/
  // Beginning of automatic wires (for undeclared instantiated-module outputs)
  wire			io_error;		// From pwt of PacketWriterTestbench.v
  wire			io_sendPacket_ready;	// From pwt of PacketWriterTestbench.v
  wire [8:0]		io_writePageCount_bits;	// From pwt of PacketWriterTestbench.v
  wire			io_writePageCount_valid;// From pwt of PacketWriterTestbench.v
  wire [7:0]		io_writeReqOut_bits_data_0;// From pwt of PacketWriterTestbench.v
  wire [7:0]		io_writeReqOut_bits_data_1;// From pwt of PacketWriterTestbench.v
  wire [1:0]		io_writeReqOut_bits_line;// From pwt of PacketWriterTestbench.v
  wire [1:0]		io_writeReqOut_bits_page_pageNum;// From pwt of PacketWriterTestbench.v
  wire			io_writeReqOut_bits_slot;// From pwt of PacketWriterTestbench.v
  wire			io_writeReqOut_valid;	// From pwt of PacketWriterTestbench.v
  // End of automatics
  
  PacketWriterTestbench pwt
    (/*AUTOINST*/
     // Outputs
     .io_sendPacket_ready		(io_sendPacket_ready),
     .io_writeReqOut_valid		(io_writeReqOut_valid),
     .io_writeReqOut_bits_slot		(io_writeReqOut_bits_slot),
     .io_writeReqOut_bits_page_pageNum	(io_writeReqOut_bits_page_pageNum[1:0]),
     .io_writeReqOut_bits_line		(io_writeReqOut_bits_line[1:0]),
     .io_writeReqOut_bits_data_0	(io_writeReqOut_bits_data_0[7:0]),
     .io_writeReqOut_bits_data_1	(io_writeReqOut_bits_data_1[7:0]),
     .io_error				(io_error),
     .io_writePageCount_valid		(io_writePageCount_valid),
     .io_writePageCount_bits		(io_writePageCount_bits[8:0]),
     // Inputs
     .clock				(clock),
     .reset				(reset),
     .io_sendPacket_valid		(io_sendPacket_valid),
     .io_sendPacket_bits_length		(io_sendPacket_bits_length[15:0]),
     .io_sendPacket_bits_pid		(io_sendPacket_bits_pid[15:0]),
     .io_sendPacket_bits_packetGood	(io_sendPacket_bits_packetGood));

  always
    begin
      clock = 0;
      #5;
      clock = 1;
      #5;
    end

  initial
    begin
      $dumpfile("testbench.vcd");
      $dumpvars;
      reset = 1;
      io_sendPacket_valid = 0;
      io_sendPacket_bits_length = 0;
      io_sendPacket_bits_pid = 0;
      io_sendPacket_bits_packetGood = 1;
      
      @(negedge clock);
      reset = 0;
      @(negedge clock);
      io_sendPacket_valid = 1;
      io_sendPacket_bits_length = 128;
      @(negedge clock);
      io_sendPacket_valid = 0;
      #5000;
      $finish;
    end
  
endmodule // bench
// Local Variables:
// verilog-library-directories:("..")
// End:
