module bench;
  reg clock;
  reg reset;  

  /*AUTOWIRE*/
  // Beginning of automatic wires (for undeclared instantiated-module outputs)
  wire [1:0]		io_destIn_0_bits_dest;	// From in_driver0 of dataInDriver.v
  wire			io_destIn_0_valid;	// From in_driver0 of dataInDriver.v
  wire [1:0]		io_destIn_1_bits_dest;	// From in_driver1 of dataInDriver.v
  wire			io_destIn_1_valid;	// From in_driver1 of dataInDriver.v
  wire [1:0]		io_destIn_2_bits_dest;	// From in_driver2 of dataInDriver.v
  wire			io_destIn_2_valid;	// From in_driver2 of dataInDriver.v
  wire [1:0]		io_destIn_3_bits_dest;	// From in_driver3 of dataInDriver.v
  wire			io_destIn_3_valid;	// From in_driver3 of dataInDriver.v
  wire [1:0]		io_portDataIn_0_bits_code_code;// From in_driver0 of dataInDriver.v
  wire [2:0]		io_portDataIn_0_bits_count;// From in_driver0 of dataInDriver.v
  wire [7:0]		io_portDataIn_0_bits_data_0;// From in_driver0 of dataInDriver.v
  wire [7:0]		io_portDataIn_0_bits_data_1;// From in_driver0 of dataInDriver.v
  wire [7:0]		io_portDataIn_0_bits_data_2;// From in_driver0 of dataInDriver.v
  wire [7:0]		io_portDataIn_0_bits_data_3;// From in_driver0 of dataInDriver.v
  wire [7:0]		io_portDataIn_0_bits_data_4;// From in_driver0 of dataInDriver.v
  wire [7:0]		io_portDataIn_0_bits_data_5;// From in_driver0 of dataInDriver.v
  wire [7:0]		io_portDataIn_0_bits_data_6;// From in_driver0 of dataInDriver.v
  wire [7:0]		io_portDataIn_0_bits_data_7;// From in_driver0 of dataInDriver.v
  wire			io_portDataIn_0_ready;	// From buffer of FlatPacketBufferComplex.v
  wire			io_portDataIn_0_valid;	// From in_driver0 of dataInDriver.v
  wire [1:0]		io_portDataIn_1_bits_code_code;// From in_driver1 of dataInDriver.v
  wire [2:0]		io_portDataIn_1_bits_count;// From in_driver1 of dataInDriver.v
  wire [7:0]		io_portDataIn_1_bits_data_0;// From in_driver1 of dataInDriver.v
  wire [7:0]		io_portDataIn_1_bits_data_1;// From in_driver1 of dataInDriver.v
  wire [7:0]		io_portDataIn_1_bits_data_2;// From in_driver1 of dataInDriver.v
  wire [7:0]		io_portDataIn_1_bits_data_3;// From in_driver1 of dataInDriver.v
  wire [7:0]		io_portDataIn_1_bits_data_4;// From in_driver1 of dataInDriver.v
  wire [7:0]		io_portDataIn_1_bits_data_5;// From in_driver1 of dataInDriver.v
  wire [7:0]		io_portDataIn_1_bits_data_6;// From in_driver1 of dataInDriver.v
  wire [7:0]		io_portDataIn_1_bits_data_7;// From in_driver1 of dataInDriver.v
  wire			io_portDataIn_1_ready;	// From buffer of FlatPacketBufferComplex.v
  wire			io_portDataIn_1_valid;	// From in_driver1 of dataInDriver.v
  wire [1:0]		io_portDataIn_2_bits_code_code;// From in_driver2 of dataInDriver.v
  wire [2:0]		io_portDataIn_2_bits_count;// From in_driver2 of dataInDriver.v
  wire [7:0]		io_portDataIn_2_bits_data_0;// From in_driver2 of dataInDriver.v
  wire [7:0]		io_portDataIn_2_bits_data_1;// From in_driver2 of dataInDriver.v
  wire [7:0]		io_portDataIn_2_bits_data_2;// From in_driver2 of dataInDriver.v
  wire [7:0]		io_portDataIn_2_bits_data_3;// From in_driver2 of dataInDriver.v
  wire [7:0]		io_portDataIn_2_bits_data_4;// From in_driver2 of dataInDriver.v
  wire [7:0]		io_portDataIn_2_bits_data_5;// From in_driver2 of dataInDriver.v
  wire [7:0]		io_portDataIn_2_bits_data_6;// From in_driver2 of dataInDriver.v
  wire [7:0]		io_portDataIn_2_bits_data_7;// From in_driver2 of dataInDriver.v
  wire			io_portDataIn_2_ready;	// From buffer of FlatPacketBufferComplex.v
  wire			io_portDataIn_2_valid;	// From in_driver2 of dataInDriver.v
  wire [1:0]		io_portDataIn_3_bits_code_code;// From in_driver3 of dataInDriver.v
  wire [2:0]		io_portDataIn_3_bits_count;// From in_driver3 of dataInDriver.v
  wire [7:0]		io_portDataIn_3_bits_data_0;// From in_driver3 of dataInDriver.v
  wire [7:0]		io_portDataIn_3_bits_data_1;// From in_driver3 of dataInDriver.v
  wire [7:0]		io_portDataIn_3_bits_data_2;// From in_driver3 of dataInDriver.v
  wire [7:0]		io_portDataIn_3_bits_data_3;// From in_driver3 of dataInDriver.v
  wire [7:0]		io_portDataIn_3_bits_data_4;// From in_driver3 of dataInDriver.v
  wire [7:0]		io_portDataIn_3_bits_data_5;// From in_driver3 of dataInDriver.v
  wire [7:0]		io_portDataIn_3_bits_data_6;// From in_driver3 of dataInDriver.v
  wire [7:0]		io_portDataIn_3_bits_data_7;// From in_driver3 of dataInDriver.v
  wire			io_portDataIn_3_ready;	// From buffer of FlatPacketBufferComplex.v
  wire			io_portDataIn_3_valid;	// From in_driver3 of dataInDriver.v
  wire [1:0]		io_portDataOut_0_bits_code_code;// From buffer of FlatPacketBufferComplex.v
  wire [2:0]		io_portDataOut_0_bits_count;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_0_bits_data_0;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_0_bits_data_1;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_0_bits_data_2;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_0_bits_data_3;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_0_bits_data_4;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_0_bits_data_5;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_0_bits_data_6;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_0_bits_data_7;// From buffer of FlatPacketBufferComplex.v
  wire			io_portDataOut_0_ready;	// From out_driver0 of dataOutDriver.v
  wire			io_portDataOut_0_valid;	// From buffer of FlatPacketBufferComplex.v
  wire [1:0]		io_portDataOut_1_bits_code_code;// From buffer of FlatPacketBufferComplex.v
  wire [2:0]		io_portDataOut_1_bits_count;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_1_bits_data_0;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_1_bits_data_1;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_1_bits_data_2;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_1_bits_data_3;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_1_bits_data_4;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_1_bits_data_5;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_1_bits_data_6;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_1_bits_data_7;// From buffer of FlatPacketBufferComplex.v
  wire			io_portDataOut_1_ready;	// From out_driver1 of dataOutDriver.v
  wire			io_portDataOut_1_valid;	// From buffer of FlatPacketBufferComplex.v
  wire [1:0]		io_portDataOut_2_bits_code_code;// From buffer of FlatPacketBufferComplex.v
  wire [2:0]		io_portDataOut_2_bits_count;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_2_bits_data_0;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_2_bits_data_1;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_2_bits_data_2;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_2_bits_data_3;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_2_bits_data_4;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_2_bits_data_5;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_2_bits_data_6;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_2_bits_data_7;// From buffer of FlatPacketBufferComplex.v
  wire			io_portDataOut_2_ready;	// From driver2 of dataOutDriver.v
  wire			io_portDataOut_2_valid;	// From buffer of FlatPacketBufferComplex.v
  wire [1:0]		io_portDataOut_3_bits_code_code;// From buffer of FlatPacketBufferComplex.v
  wire [2:0]		io_portDataOut_3_bits_count;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_3_bits_data_0;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_3_bits_data_1;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_3_bits_data_2;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_3_bits_data_3;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_3_bits_data_4;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_3_bits_data_5;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_3_bits_data_6;// From buffer of FlatPacketBufferComplex.v
  wire [7:0]		io_portDataOut_3_bits_data_7;// From buffer of FlatPacketBufferComplex.v
  wire			io_portDataOut_3_ready;	// From out_driver3 of dataOutDriver.v
  wire			io_portDataOut_3_valid;	// From buffer of FlatPacketBufferComplex.v
  // End of automatics

  /* FlatPacketBufferComplex AUTO_TEMPLATE
   (
   );
   */
  FlatPacketBufferComplex buffer
    (/*AUTOINST*/
     // Outputs
     .io_portDataOut_0_valid		(io_portDataOut_0_valid),
     .io_portDataOut_0_bits_data_0	(io_portDataOut_0_bits_data_0[7:0]),
     .io_portDataOut_0_bits_data_1	(io_portDataOut_0_bits_data_1[7:0]),
     .io_portDataOut_0_bits_data_2	(io_portDataOut_0_bits_data_2[7:0]),
     .io_portDataOut_0_bits_data_3	(io_portDataOut_0_bits_data_3[7:0]),
     .io_portDataOut_0_bits_data_4	(io_portDataOut_0_bits_data_4[7:0]),
     .io_portDataOut_0_bits_data_5	(io_portDataOut_0_bits_data_5[7:0]),
     .io_portDataOut_0_bits_data_6	(io_portDataOut_0_bits_data_6[7:0]),
     .io_portDataOut_0_bits_data_7	(io_portDataOut_0_bits_data_7[7:0]),
     .io_portDataOut_0_bits_count	(io_portDataOut_0_bits_count[2:0]),
     .io_portDataOut_0_bits_code_code	(io_portDataOut_0_bits_code_code[1:0]),
     .io_portDataOut_1_valid		(io_portDataOut_1_valid),
     .io_portDataOut_1_bits_data_0	(io_portDataOut_1_bits_data_0[7:0]),
     .io_portDataOut_1_bits_data_1	(io_portDataOut_1_bits_data_1[7:0]),
     .io_portDataOut_1_bits_data_2	(io_portDataOut_1_bits_data_2[7:0]),
     .io_portDataOut_1_bits_data_3	(io_portDataOut_1_bits_data_3[7:0]),
     .io_portDataOut_1_bits_data_4	(io_portDataOut_1_bits_data_4[7:0]),
     .io_portDataOut_1_bits_data_5	(io_portDataOut_1_bits_data_5[7:0]),
     .io_portDataOut_1_bits_data_6	(io_portDataOut_1_bits_data_6[7:0]),
     .io_portDataOut_1_bits_data_7	(io_portDataOut_1_bits_data_7[7:0]),
     .io_portDataOut_1_bits_count	(io_portDataOut_1_bits_count[2:0]),
     .io_portDataOut_1_bits_code_code	(io_portDataOut_1_bits_code_code[1:0]),
     .io_portDataOut_2_valid		(io_portDataOut_2_valid),
     .io_portDataOut_2_bits_data_0	(io_portDataOut_2_bits_data_0[7:0]),
     .io_portDataOut_2_bits_data_1	(io_portDataOut_2_bits_data_1[7:0]),
     .io_portDataOut_2_bits_data_2	(io_portDataOut_2_bits_data_2[7:0]),
     .io_portDataOut_2_bits_data_3	(io_portDataOut_2_bits_data_3[7:0]),
     .io_portDataOut_2_bits_data_4	(io_portDataOut_2_bits_data_4[7:0]),
     .io_portDataOut_2_bits_data_5	(io_portDataOut_2_bits_data_5[7:0]),
     .io_portDataOut_2_bits_data_6	(io_portDataOut_2_bits_data_6[7:0]),
     .io_portDataOut_2_bits_data_7	(io_portDataOut_2_bits_data_7[7:0]),
     .io_portDataOut_2_bits_count	(io_portDataOut_2_bits_count[2:0]),
     .io_portDataOut_2_bits_code_code	(io_portDataOut_2_bits_code_code[1:0]),
     .io_portDataOut_3_valid		(io_portDataOut_3_valid),
     .io_portDataOut_3_bits_data_0	(io_portDataOut_3_bits_data_0[7:0]),
     .io_portDataOut_3_bits_data_1	(io_portDataOut_3_bits_data_1[7:0]),
     .io_portDataOut_3_bits_data_2	(io_portDataOut_3_bits_data_2[7:0]),
     .io_portDataOut_3_bits_data_3	(io_portDataOut_3_bits_data_3[7:0]),
     .io_portDataOut_3_bits_data_4	(io_portDataOut_3_bits_data_4[7:0]),
     .io_portDataOut_3_bits_data_5	(io_portDataOut_3_bits_data_5[7:0]),
     .io_portDataOut_3_bits_data_6	(io_portDataOut_3_bits_data_6[7:0]),
     .io_portDataOut_3_bits_data_7	(io_portDataOut_3_bits_data_7[7:0]),
     .io_portDataOut_3_bits_count	(io_portDataOut_3_bits_count[2:0]),
     .io_portDataOut_3_bits_code_code	(io_portDataOut_3_bits_code_code[1:0]),
     .io_portDataIn_0_ready		(io_portDataIn_0_ready),
     .io_portDataIn_1_ready		(io_portDataIn_1_ready),
     .io_portDataIn_2_ready		(io_portDataIn_2_ready),
     .io_portDataIn_3_ready		(io_portDataIn_3_ready),
     // Inputs
     .clock				(clock),
     .reset				(reset),
     .io_portDataOut_0_ready		(io_portDataOut_0_ready),
     .io_portDataOut_1_ready		(io_portDataOut_1_ready),
     .io_portDataOut_2_ready		(io_portDataOut_2_ready),
     .io_portDataOut_3_ready		(io_portDataOut_3_ready),
     .io_portDataIn_0_valid		(io_portDataIn_0_valid),
     .io_portDataIn_0_bits_data_0	(io_portDataIn_0_bits_data_0[7:0]),
     .io_portDataIn_0_bits_data_1	(io_portDataIn_0_bits_data_1[7:0]),
     .io_portDataIn_0_bits_data_2	(io_portDataIn_0_bits_data_2[7:0]),
     .io_portDataIn_0_bits_data_3	(io_portDataIn_0_bits_data_3[7:0]),
     .io_portDataIn_0_bits_data_4	(io_portDataIn_0_bits_data_4[7:0]),
     .io_portDataIn_0_bits_data_5	(io_portDataIn_0_bits_data_5[7:0]),
     .io_portDataIn_0_bits_data_6	(io_portDataIn_0_bits_data_6[7:0]),
     .io_portDataIn_0_bits_data_7	(io_portDataIn_0_bits_data_7[7:0]),
     .io_portDataIn_0_bits_count	(io_portDataIn_0_bits_count[2:0]),
     .io_portDataIn_0_bits_code_code	(io_portDataIn_0_bits_code_code[1:0]),
     .io_portDataIn_1_valid		(io_portDataIn_1_valid),
     .io_portDataIn_1_bits_data_0	(io_portDataIn_1_bits_data_0[7:0]),
     .io_portDataIn_1_bits_data_1	(io_portDataIn_1_bits_data_1[7:0]),
     .io_portDataIn_1_bits_data_2	(io_portDataIn_1_bits_data_2[7:0]),
     .io_portDataIn_1_bits_data_3	(io_portDataIn_1_bits_data_3[7:0]),
     .io_portDataIn_1_bits_data_4	(io_portDataIn_1_bits_data_4[7:0]),
     .io_portDataIn_1_bits_data_5	(io_portDataIn_1_bits_data_5[7:0]),
     .io_portDataIn_1_bits_data_6	(io_portDataIn_1_bits_data_6[7:0]),
     .io_portDataIn_1_bits_data_7	(io_portDataIn_1_bits_data_7[7:0]),
     .io_portDataIn_1_bits_count	(io_portDataIn_1_bits_count[2:0]),
     .io_portDataIn_1_bits_code_code	(io_portDataIn_1_bits_code_code[1:0]),
     .io_portDataIn_2_valid		(io_portDataIn_2_valid),
     .io_portDataIn_2_bits_data_0	(io_portDataIn_2_bits_data_0[7:0]),
     .io_portDataIn_2_bits_data_1	(io_portDataIn_2_bits_data_1[7:0]),
     .io_portDataIn_2_bits_data_2	(io_portDataIn_2_bits_data_2[7:0]),
     .io_portDataIn_2_bits_data_3	(io_portDataIn_2_bits_data_3[7:0]),
     .io_portDataIn_2_bits_data_4	(io_portDataIn_2_bits_data_4[7:0]),
     .io_portDataIn_2_bits_data_5	(io_portDataIn_2_bits_data_5[7:0]),
     .io_portDataIn_2_bits_data_6	(io_portDataIn_2_bits_data_6[7:0]),
     .io_portDataIn_2_bits_data_7	(io_portDataIn_2_bits_data_7[7:0]),
     .io_portDataIn_2_bits_count	(io_portDataIn_2_bits_count[2:0]),
     .io_portDataIn_2_bits_code_code	(io_portDataIn_2_bits_code_code[1:0]),
     .io_portDataIn_3_valid		(io_portDataIn_3_valid),
     .io_portDataIn_3_bits_data_0	(io_portDataIn_3_bits_data_0[7:0]),
     .io_portDataIn_3_bits_data_1	(io_portDataIn_3_bits_data_1[7:0]),
     .io_portDataIn_3_bits_data_2	(io_portDataIn_3_bits_data_2[7:0]),
     .io_portDataIn_3_bits_data_3	(io_portDataIn_3_bits_data_3[7:0]),
     .io_portDataIn_3_bits_data_4	(io_portDataIn_3_bits_data_4[7:0]),
     .io_portDataIn_3_bits_data_5	(io_portDataIn_3_bits_data_5[7:0]),
     .io_portDataIn_3_bits_data_6	(io_portDataIn_3_bits_data_6[7:0]),
     .io_portDataIn_3_bits_data_7	(io_portDataIn_3_bits_data_7[7:0]),
     .io_portDataIn_3_bits_count	(io_portDataIn_3_bits_count[2:0]),
     .io_portDataIn_3_bits_code_code	(io_portDataIn_3_bits_code_code[1:0]),
     .io_destIn_0_valid			(io_destIn_0_valid),
     .io_destIn_0_bits_dest		(io_destIn_0_bits_dest[1:0]),
     .io_destIn_1_valid			(io_destIn_1_valid),
     .io_destIn_1_bits_dest		(io_destIn_1_bits_dest[1:0]),
     .io_destIn_2_valid			(io_destIn_2_valid),
     .io_destIn_2_bits_dest		(io_destIn_2_bits_dest[1:0]),
     .io_destIn_3_valid			(io_destIn_3_valid),
     .io_destIn_3_bits_dest		(io_destIn_3_bits_dest[1:0]));

  /* dataInDriver AUTO_TEMPLATE
   (
    .io_portIn_\(.*\)  (io_portDataIn_@_\1[]),
    .io_destIn_\(.*\)   (io_destIn_@_\1[]),
   );
   */
  dataInDriver in_driver0
    (/*AUTOINST*/
     // Outputs
     .io_portIn_valid			(io_portDataIn_0_valid), // Templated
     .io_portIn_bits_data_0		(io_portDataIn_0_bits_data_0[7:0]), // Templated
     .io_portIn_bits_data_1		(io_portDataIn_0_bits_data_1[7:0]), // Templated
     .io_portIn_bits_data_2		(io_portDataIn_0_bits_data_2[7:0]), // Templated
     .io_portIn_bits_data_3		(io_portDataIn_0_bits_data_3[7:0]), // Templated
     .io_portIn_bits_data_4		(io_portDataIn_0_bits_data_4[7:0]), // Templated
     .io_portIn_bits_data_5		(io_portDataIn_0_bits_data_5[7:0]), // Templated
     .io_portIn_bits_data_6		(io_portDataIn_0_bits_data_6[7:0]), // Templated
     .io_portIn_bits_data_7		(io_portDataIn_0_bits_data_7[7:0]), // Templated
     .io_portIn_bits_count		(io_portDataIn_0_bits_count[2:0]), // Templated
     .io_portIn_bits_code_code		(io_portDataIn_0_bits_code_code[1:0]), // Templated
     .io_destIn_valid			(io_destIn_0_valid),	 // Templated
     .io_destIn_bits_dest		(io_destIn_0_bits_dest[1:0]), // Templated
     // Inputs
     .clock				(clock),
     .reset				(reset),
     .io_portIn_ready			(io_portDataIn_0_ready)); // Templated
  
   dataInDriver in_driver1
     (/*AUTOINST*/
      // Outputs
      .io_portIn_valid			(io_portDataIn_1_valid), // Templated
      .io_portIn_bits_data_0		(io_portDataIn_1_bits_data_0[7:0]), // Templated
      .io_portIn_bits_data_1		(io_portDataIn_1_bits_data_1[7:0]), // Templated
      .io_portIn_bits_data_2		(io_portDataIn_1_bits_data_2[7:0]), // Templated
      .io_portIn_bits_data_3		(io_portDataIn_1_bits_data_3[7:0]), // Templated
      .io_portIn_bits_data_4		(io_portDataIn_1_bits_data_4[7:0]), // Templated
      .io_portIn_bits_data_5		(io_portDataIn_1_bits_data_5[7:0]), // Templated
      .io_portIn_bits_data_6		(io_portDataIn_1_bits_data_6[7:0]), // Templated
      .io_portIn_bits_data_7		(io_portDataIn_1_bits_data_7[7:0]), // Templated
      .io_portIn_bits_count		(io_portDataIn_1_bits_count[2:0]), // Templated
      .io_portIn_bits_code_code		(io_portDataIn_1_bits_code_code[1:0]), // Templated
      .io_destIn_valid			(io_destIn_1_valid),	 // Templated
      .io_destIn_bits_dest		(io_destIn_1_bits_dest[1:0]), // Templated
      // Inputs
      .clock				(clock),
      .reset				(reset),
      .io_portIn_ready			(io_portDataIn_1_ready)); // Templated
     
   dataInDriver in_driver2
     (/*AUTOINST*/
      // Outputs
      .io_portIn_valid			(io_portDataIn_2_valid), // Templated
      .io_portIn_bits_data_0		(io_portDataIn_2_bits_data_0[7:0]), // Templated
      .io_portIn_bits_data_1		(io_portDataIn_2_bits_data_1[7:0]), // Templated
      .io_portIn_bits_data_2		(io_portDataIn_2_bits_data_2[7:0]), // Templated
      .io_portIn_bits_data_3		(io_portDataIn_2_bits_data_3[7:0]), // Templated
      .io_portIn_bits_data_4		(io_portDataIn_2_bits_data_4[7:0]), // Templated
      .io_portIn_bits_data_5		(io_portDataIn_2_bits_data_5[7:0]), // Templated
      .io_portIn_bits_data_6		(io_portDataIn_2_bits_data_6[7:0]), // Templated
      .io_portIn_bits_data_7		(io_portDataIn_2_bits_data_7[7:0]), // Templated
      .io_portIn_bits_count		(io_portDataIn_2_bits_count[2:0]), // Templated
      .io_portIn_bits_code_code		(io_portDataIn_2_bits_code_code[1:0]), // Templated
      .io_destIn_valid			(io_destIn_2_valid),	 // Templated
      .io_destIn_bits_dest		(io_destIn_2_bits_dest[1:0]), // Templated
      // Inputs
      .clock				(clock),
      .reset				(reset),
      .io_portIn_ready			(io_portDataIn_2_ready)); // Templated
     
   dataInDriver in_driver3
     (/*AUTOINST*/
      // Outputs
      .io_portIn_valid			(io_portDataIn_3_valid), // Templated
      .io_portIn_bits_data_0		(io_portDataIn_3_bits_data_0[7:0]), // Templated
      .io_portIn_bits_data_1		(io_portDataIn_3_bits_data_1[7:0]), // Templated
      .io_portIn_bits_data_2		(io_portDataIn_3_bits_data_2[7:0]), // Templated
      .io_portIn_bits_data_3		(io_portDataIn_3_bits_data_3[7:0]), // Templated
      .io_portIn_bits_data_4		(io_portDataIn_3_bits_data_4[7:0]), // Templated
      .io_portIn_bits_data_5		(io_portDataIn_3_bits_data_5[7:0]), // Templated
      .io_portIn_bits_data_6		(io_portDataIn_3_bits_data_6[7:0]), // Templated
      .io_portIn_bits_data_7		(io_portDataIn_3_bits_data_7[7:0]), // Templated
      .io_portIn_bits_count		(io_portDataIn_3_bits_count[2:0]), // Templated
      .io_portIn_bits_code_code		(io_portDataIn_3_bits_code_code[1:0]), // Templated
      .io_destIn_valid			(io_destIn_3_valid),	 // Templated
      .io_destIn_bits_dest		(io_destIn_3_bits_dest[1:0]), // Templated
      // Inputs
      .clock				(clock),
      .reset				(reset),
      .io_portIn_ready			(io_portDataIn_3_ready)); // Templated
     
 /* dataOutDriver AUTO_TEMPLATE
   (
    .io_portOut_\(.*\)  (io_portDataOut_@_\1[]),
   );
   */
  dataOutDriver out_driver0
    (/*AUTOINST*/
     // Outputs
     .io_portOut_ready			(io_portDataOut_0_ready), // Templated
     // Inputs
     .clock				(clock),
     .reset				(reset),
     .io_portOut_valid			(io_portDataOut_0_valid), // Templated
     .io_portOut_bits_data_0		(io_portDataOut_0_bits_data_0[7:0]), // Templated
     .io_portOut_bits_data_1		(io_portDataOut_0_bits_data_1[7:0]), // Templated
     .io_portOut_bits_data_2		(io_portDataOut_0_bits_data_2[7:0]), // Templated
     .io_portOut_bits_data_3		(io_portDataOut_0_bits_data_3[7:0]), // Templated
     .io_portOut_bits_data_4		(io_portDataOut_0_bits_data_4[7:0]), // Templated
     .io_portOut_bits_data_5		(io_portDataOut_0_bits_data_5[7:0]), // Templated
     .io_portOut_bits_data_6		(io_portDataOut_0_bits_data_6[7:0]), // Templated
     .io_portOut_bits_data_7		(io_portDataOut_0_bits_data_7[7:0]), // Templated
     .io_portOut_bits_count		(io_portDataOut_0_bits_count[2:0]), // Templated
     .io_portOut_bits_code_code		(io_portDataOut_0_bits_code_code[1:0])); // Templated
  

 dataOutDriver out_driver1
    (/*AUTOINST*/
     // Outputs
     .io_portOut_ready			(io_portDataOut_1_ready), // Templated
     // Inputs
     .clock				(clock),
     .reset				(reset),
     .io_portOut_valid			(io_portDataOut_1_valid), // Templated
     .io_portOut_bits_data_0		(io_portDataOut_1_bits_data_0[7:0]), // Templated
     .io_portOut_bits_data_1		(io_portDataOut_1_bits_data_1[7:0]), // Templated
     .io_portOut_bits_data_2		(io_portDataOut_1_bits_data_2[7:0]), // Templated
     .io_portOut_bits_data_3		(io_portDataOut_1_bits_data_3[7:0]), // Templated
     .io_portOut_bits_data_4		(io_portDataOut_1_bits_data_4[7:0]), // Templated
     .io_portOut_bits_data_5		(io_portDataOut_1_bits_data_5[7:0]), // Templated
     .io_portOut_bits_data_6		(io_portDataOut_1_bits_data_6[7:0]), // Templated
     .io_portOut_bits_data_7		(io_portDataOut_1_bits_data_7[7:0]), // Templated
     .io_portOut_bits_count		(io_portDataOut_1_bits_count[2:0]), // Templated
     .io_portOut_bits_code_code		(io_portDataOut_1_bits_code_code[1:0])); // Templated
  
 dataOutDriver driver2
    (/*AUTOINST*/
     // Outputs
     .io_portOut_ready			(io_portDataOut_2_ready), // Templated
     // Inputs
     .clock				(clock),
     .reset				(reset),
     .io_portOut_valid			(io_portDataOut_2_valid), // Templated
     .io_portOut_bits_data_0		(io_portDataOut_2_bits_data_0[7:0]), // Templated
     .io_portOut_bits_data_1		(io_portDataOut_2_bits_data_1[7:0]), // Templated
     .io_portOut_bits_data_2		(io_portDataOut_2_bits_data_2[7:0]), // Templated
     .io_portOut_bits_data_3		(io_portDataOut_2_bits_data_3[7:0]), // Templated
     .io_portOut_bits_data_4		(io_portDataOut_2_bits_data_4[7:0]), // Templated
     .io_portOut_bits_data_5		(io_portDataOut_2_bits_data_5[7:0]), // Templated
     .io_portOut_bits_data_6		(io_portDataOut_2_bits_data_6[7:0]), // Templated
     .io_portOut_bits_data_7		(io_portDataOut_2_bits_data_7[7:0]), // Templated
     .io_portOut_bits_count		(io_portDataOut_2_bits_count[2:0]), // Templated
     .io_portOut_bits_code_code		(io_portDataOut_2_bits_code_code[1:0])); // Templated
  
 dataOutDriver out_driver3
    (/*AUTOINST*/
     // Outputs
     .io_portOut_ready			(io_portDataOut_3_ready), // Templated
     // Inputs
     .clock				(clock),
     .reset				(reset),
     .io_portOut_valid			(io_portDataOut_3_valid), // Templated
     .io_portOut_bits_data_0		(io_portDataOut_3_bits_data_0[7:0]), // Templated
     .io_portOut_bits_data_1		(io_portDataOut_3_bits_data_1[7:0]), // Templated
     .io_portOut_bits_data_2		(io_portDataOut_3_bits_data_2[7:0]), // Templated
     .io_portOut_bits_data_3		(io_portDataOut_3_bits_data_3[7:0]), // Templated
     .io_portOut_bits_data_4		(io_portDataOut_3_bits_data_4[7:0]), // Templated
     .io_portOut_bits_data_5		(io_portDataOut_3_bits_data_5[7:0]), // Templated
     .io_portOut_bits_data_6		(io_portDataOut_3_bits_data_6[7:0]), // Templated
     .io_portOut_bits_data_7		(io_portDataOut_3_bits_data_7[7:0]), // Templated
     .io_portOut_bits_count		(io_portDataOut_3_bits_count[2:0]), // Templated
     .io_portOut_bits_code_code		(io_portDataOut_3_bits_code_code[1:0])); // Templated

  initial
    begin
      $dumpfile("bench.vcd");
      $dumpvars;
      reset = 1;
      #200;
      reset = 0;
      #20000;
      $finish;
    end

  always
    begin
      clock = 0; #5;
      clock = 1; #5;
    end
  
endmodule // bench
// Local Variables:
// verilog-library-directories:("." "generated")
// End:
