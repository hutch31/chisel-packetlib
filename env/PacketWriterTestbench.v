module Queue(
  input         clock,
  input         reset,
  output        io_enq_ready,
  input         io_enq_valid,
  input  [15:0] io_enq_bits_length,
  input         io_enq_bits_packetGood,
  input         io_deq_ready,
  output        io_deq_valid,
  output [15:0] io_deq_bits_length,
  output        io_deq_bits_packetGood
);
`ifdef RANDOMIZE_MEM_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_4;
`endif // RANDOMIZE_REG_INIT
  reg [15:0] ram_length [0:3]; // @[Decoupled.scala 218:16]
  wire [15:0] ram_length_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire [1:0] ram_length_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [15:0] ram_length_MPORT_data; // @[Decoupled.scala 218:16]
  wire [1:0] ram_length_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_length_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_length_MPORT_en; // @[Decoupled.scala 218:16]
  reg  ram_packetGood [0:3]; // @[Decoupled.scala 218:16]
  wire  ram_packetGood_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire [1:0] ram_packetGood_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_packetGood_MPORT_data; // @[Decoupled.scala 218:16]
  wire [1:0] ram_packetGood_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_packetGood_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_packetGood_MPORT_en; // @[Decoupled.scala 218:16]
  reg [1:0] enq_ptr_value; // @[Counter.scala 60:40]
  reg [1:0] deq_ptr_value; // @[Counter.scala 60:40]
  reg  maybe_full; // @[Decoupled.scala 221:27]
  wire  ptr_match = enq_ptr_value == deq_ptr_value; // @[Decoupled.scala 223:33]
  wire  empty = ptr_match & ~maybe_full; // @[Decoupled.scala 224:25]
  wire  full = ptr_match & maybe_full; // @[Decoupled.scala 225:24]
  wire  do_enq = io_enq_ready & io_enq_valid; // @[Decoupled.scala 40:37]
  wire  do_deq = io_deq_ready & io_deq_valid; // @[Decoupled.scala 40:37]
  wire [1:0] _enq_ptr_value_T_1 = enq_ptr_value + 2'h1; // @[Counter.scala 76:24]
  wire [1:0] _deq_ptr_value_T_1 = deq_ptr_value + 2'h1; // @[Counter.scala 76:24]
  assign ram_length_io_deq_bits_MPORT_addr = deq_ptr_value;
  assign ram_length_io_deq_bits_MPORT_data = ram_length[ram_length_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_length_MPORT_data = io_enq_bits_length;
  assign ram_length_MPORT_addr = enq_ptr_value;
  assign ram_length_MPORT_mask = 1'h1;
  assign ram_length_MPORT_en = io_enq_ready & io_enq_valid;
  assign ram_packetGood_io_deq_bits_MPORT_addr = deq_ptr_value;
  assign ram_packetGood_io_deq_bits_MPORT_data = ram_packetGood[ram_packetGood_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_packetGood_MPORT_data = io_enq_bits_packetGood;
  assign ram_packetGood_MPORT_addr = enq_ptr_value;
  assign ram_packetGood_MPORT_mask = 1'h1;
  assign ram_packetGood_MPORT_en = io_enq_ready & io_enq_valid;
  assign io_enq_ready = ~full; // @[Decoupled.scala 241:19]
  assign io_deq_valid = ~empty; // @[Decoupled.scala 240:19]
  assign io_deq_bits_length = ram_length_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  assign io_deq_bits_packetGood = ram_packetGood_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  always @(posedge clock) begin
    if(ram_length_MPORT_en & ram_length_MPORT_mask) begin
      ram_length[ram_length_MPORT_addr] <= ram_length_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if(ram_packetGood_MPORT_en & ram_packetGood_MPORT_mask) begin
      ram_packetGood[ram_packetGood_MPORT_addr] <= ram_packetGood_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if (reset) begin // @[Counter.scala 60:40]
      enq_ptr_value <= 2'h0; // @[Counter.scala 60:40]
    end else if (do_enq) begin // @[Decoupled.scala 229:17]
      enq_ptr_value <= _enq_ptr_value_T_1; // @[Counter.scala 76:15]
    end
    if (reset) begin // @[Counter.scala 60:40]
      deq_ptr_value <= 2'h0; // @[Counter.scala 60:40]
    end else if (do_deq) begin // @[Decoupled.scala 233:17]
      deq_ptr_value <= _deq_ptr_value_T_1; // @[Counter.scala 76:15]
    end
    if (reset) begin // @[Decoupled.scala 221:27]
      maybe_full <= 1'h0; // @[Decoupled.scala 221:27]
    end else if (do_enq != do_deq) begin // @[Decoupled.scala 236:28]
      maybe_full <= do_enq; // @[Decoupled.scala 237:16]
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_MEM_INIT
  _RAND_0 = {1{`RANDOM}};
  for (initvar = 0; initvar < 4; initvar = initvar+1)
    ram_length[initvar] = _RAND_0[15:0];
  _RAND_1 = {1{`RANDOM}};
  for (initvar = 0; initvar < 4; initvar = initvar+1)
    ram_packetGood[initvar] = _RAND_1[0:0];
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  _RAND_2 = {1{`RANDOM}};
  enq_ptr_value = _RAND_2[1:0];
  _RAND_3 = {1{`RANDOM}};
  deq_ptr_value = _RAND_3[1:0];
  _RAND_4 = {1{`RANDOM}};
  maybe_full = _RAND_4[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module Queue_1(
  input        clock,
  input        reset,
  output       io_enq_ready,
  input        io_enq_valid,
  input  [7:0] io_enq_bits_data_0,
  input  [7:0] io_enq_bits_data_1,
  input  [7:0] io_enq_bits_data_2,
  input  [7:0] io_enq_bits_data_3,
  input  [1:0] io_enq_bits_code_code,
  input        io_deq_ready,
  output       io_deq_valid,
  output [7:0] io_deq_bits_data_0,
  output [7:0] io_deq_bits_data_1,
  output [7:0] io_deq_bits_data_2,
  output [7:0] io_deq_bits_data_3,
  output [1:0] io_deq_bits_code_code
);
`ifdef RANDOMIZE_MEM_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_4;
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_5;
  reg [31:0] _RAND_6;
  reg [31:0] _RAND_7;
`endif // RANDOMIZE_REG_INIT
  reg [7:0] ram_data_0 [0:1]; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_0_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_0_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_0_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_0_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_data_0_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_data_0_MPORT_en; // @[Decoupled.scala 218:16]
  reg [7:0] ram_data_1 [0:1]; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_1_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_1_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_1_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_1_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_data_1_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_data_1_MPORT_en; // @[Decoupled.scala 218:16]
  reg [7:0] ram_data_2 [0:1]; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_2_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_2_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_2_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_2_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_data_2_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_data_2_MPORT_en; // @[Decoupled.scala 218:16]
  reg [7:0] ram_data_3 [0:1]; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_3_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_3_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_3_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_3_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_data_3_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_data_3_MPORT_en; // @[Decoupled.scala 218:16]
  reg [1:0] ram_code_code [0:1]; // @[Decoupled.scala 218:16]
  wire [1:0] ram_code_code_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_code_code_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [1:0] ram_code_code_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_code_code_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_code_code_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_code_code_MPORT_en; // @[Decoupled.scala 218:16]
  reg  enq_ptr_value; // @[Counter.scala 60:40]
  reg  deq_ptr_value; // @[Counter.scala 60:40]
  reg  maybe_full; // @[Decoupled.scala 221:27]
  wire  ptr_match = enq_ptr_value == deq_ptr_value; // @[Decoupled.scala 223:33]
  wire  empty = ptr_match & ~maybe_full; // @[Decoupled.scala 224:25]
  wire  full = ptr_match & maybe_full; // @[Decoupled.scala 225:24]
  wire  do_enq = io_enq_ready & io_enq_valid; // @[Decoupled.scala 40:37]
  wire  do_deq = io_deq_ready & io_deq_valid; // @[Decoupled.scala 40:37]
  assign ram_data_0_io_deq_bits_MPORT_addr = deq_ptr_value;
  assign ram_data_0_io_deq_bits_MPORT_data = ram_data_0[ram_data_0_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_data_0_MPORT_data = io_enq_bits_data_0;
  assign ram_data_0_MPORT_addr = enq_ptr_value;
  assign ram_data_0_MPORT_mask = 1'h1;
  assign ram_data_0_MPORT_en = io_enq_ready & io_enq_valid;
  assign ram_data_1_io_deq_bits_MPORT_addr = deq_ptr_value;
  assign ram_data_1_io_deq_bits_MPORT_data = ram_data_1[ram_data_1_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_data_1_MPORT_data = io_enq_bits_data_1;
  assign ram_data_1_MPORT_addr = enq_ptr_value;
  assign ram_data_1_MPORT_mask = 1'h1;
  assign ram_data_1_MPORT_en = io_enq_ready & io_enq_valid;
  assign ram_data_2_io_deq_bits_MPORT_addr = deq_ptr_value;
  assign ram_data_2_io_deq_bits_MPORT_data = ram_data_2[ram_data_2_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_data_2_MPORT_data = io_enq_bits_data_2;
  assign ram_data_2_MPORT_addr = enq_ptr_value;
  assign ram_data_2_MPORT_mask = 1'h1;
  assign ram_data_2_MPORT_en = io_enq_ready & io_enq_valid;
  assign ram_data_3_io_deq_bits_MPORT_addr = deq_ptr_value;
  assign ram_data_3_io_deq_bits_MPORT_data = ram_data_3[ram_data_3_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_data_3_MPORT_data = io_enq_bits_data_3;
  assign ram_data_3_MPORT_addr = enq_ptr_value;
  assign ram_data_3_MPORT_mask = 1'h1;
  assign ram_data_3_MPORT_en = io_enq_ready & io_enq_valid;
  assign ram_code_code_io_deq_bits_MPORT_addr = deq_ptr_value;
  assign ram_code_code_io_deq_bits_MPORT_data = ram_code_code[ram_code_code_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_code_code_MPORT_data = io_enq_bits_code_code;
  assign ram_code_code_MPORT_addr = enq_ptr_value;
  assign ram_code_code_MPORT_mask = 1'h1;
  assign ram_code_code_MPORT_en = io_enq_ready & io_enq_valid;
  assign io_enq_ready = ~full; // @[Decoupled.scala 241:19]
  assign io_deq_valid = ~empty; // @[Decoupled.scala 240:19]
  assign io_deq_bits_data_0 = ram_data_0_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  assign io_deq_bits_data_1 = ram_data_1_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  assign io_deq_bits_data_2 = ram_data_2_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  assign io_deq_bits_data_3 = ram_data_3_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  assign io_deq_bits_code_code = ram_code_code_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  always @(posedge clock) begin
    if(ram_data_0_MPORT_en & ram_data_0_MPORT_mask) begin
      ram_data_0[ram_data_0_MPORT_addr] <= ram_data_0_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if(ram_data_1_MPORT_en & ram_data_1_MPORT_mask) begin
      ram_data_1[ram_data_1_MPORT_addr] <= ram_data_1_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if(ram_data_2_MPORT_en & ram_data_2_MPORT_mask) begin
      ram_data_2[ram_data_2_MPORT_addr] <= ram_data_2_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if(ram_data_3_MPORT_en & ram_data_3_MPORT_mask) begin
      ram_data_3[ram_data_3_MPORT_addr] <= ram_data_3_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if(ram_code_code_MPORT_en & ram_code_code_MPORT_mask) begin
      ram_code_code[ram_code_code_MPORT_addr] <= ram_code_code_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if (reset) begin // @[Counter.scala 60:40]
      enq_ptr_value <= 1'h0; // @[Counter.scala 60:40]
    end else if (do_enq) begin // @[Decoupled.scala 229:17]
      enq_ptr_value <= enq_ptr_value + 1'h1; // @[Counter.scala 76:15]
    end
    if (reset) begin // @[Counter.scala 60:40]
      deq_ptr_value <= 1'h0; // @[Counter.scala 60:40]
    end else if (do_deq) begin // @[Decoupled.scala 233:17]
      deq_ptr_value <= deq_ptr_value + 1'h1; // @[Counter.scala 76:15]
    end
    if (reset) begin // @[Decoupled.scala 221:27]
      maybe_full <= 1'h0; // @[Decoupled.scala 221:27]
    end else if (do_enq != do_deq) begin // @[Decoupled.scala 236:28]
      maybe_full <= do_enq; // @[Decoupled.scala 237:16]
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_MEM_INIT
  _RAND_0 = {1{`RANDOM}};
  for (initvar = 0; initvar < 2; initvar = initvar+1)
    ram_data_0[initvar] = _RAND_0[7:0];
  _RAND_1 = {1{`RANDOM}};
  for (initvar = 0; initvar < 2; initvar = initvar+1)
    ram_data_1[initvar] = _RAND_1[7:0];
  _RAND_2 = {1{`RANDOM}};
  for (initvar = 0; initvar < 2; initvar = initvar+1)
    ram_data_2[initvar] = _RAND_2[7:0];
  _RAND_3 = {1{`RANDOM}};
  for (initvar = 0; initvar < 2; initvar = initvar+1)
    ram_data_3[initvar] = _RAND_3[7:0];
  _RAND_4 = {1{`RANDOM}};
  for (initvar = 0; initvar < 2; initvar = initvar+1)
    ram_code_code[initvar] = _RAND_4[1:0];
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  _RAND_5 = {1{`RANDOM}};
  enq_ptr_value = _RAND_5[0:0];
  _RAND_6 = {1{`RANDOM}};
  deq_ptr_value = _RAND_6[0:0];
  _RAND_7 = {1{`RANDOM}};
  maybe_full = _RAND_7[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module PacketSender(
  input         clock,
  input         reset,
  input         io_packetData_ready,
  output        io_packetData_valid,
  output [7:0]  io_packetData_bits_data_0,
  output [7:0]  io_packetData_bits_data_1,
  output [7:0]  io_packetData_bits_data_2,
  output [7:0]  io_packetData_bits_data_3,
  output [1:0]  io_packetData_bits_code_code,
  output        io_sendPacket_ready,
  input         io_sendPacket_valid,
  input  [15:0] io_sendPacket_bits_length,
  input         io_sendPacket_bits_packetGood
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
`endif // RANDOMIZE_REG_INIT
  wire  info_clock; // @[PacketSender.scala 21:20]
  wire  info_reset; // @[PacketSender.scala 21:20]
  wire  info_io_enq_ready; // @[PacketSender.scala 21:20]
  wire  info_io_enq_valid; // @[PacketSender.scala 21:20]
  wire [15:0] info_io_enq_bits_length; // @[PacketSender.scala 21:20]
  wire  info_io_enq_bits_packetGood; // @[PacketSender.scala 21:20]
  wire  info_io_deq_ready; // @[PacketSender.scala 21:20]
  wire  info_io_deq_valid; // @[PacketSender.scala 21:20]
  wire [15:0] info_io_deq_bits_length; // @[PacketSender.scala 21:20]
  wire  info_io_deq_bits_packetGood; // @[PacketSender.scala 21:20]
  wire  txq_clock; // @[PacketSender.scala 22:19]
  wire  txq_reset; // @[PacketSender.scala 22:19]
  wire  txq_io_enq_ready; // @[PacketSender.scala 22:19]
  wire  txq_io_enq_valid; // @[PacketSender.scala 22:19]
  wire [7:0] txq_io_enq_bits_data_0; // @[PacketSender.scala 22:19]
  wire [7:0] txq_io_enq_bits_data_1; // @[PacketSender.scala 22:19]
  wire [7:0] txq_io_enq_bits_data_2; // @[PacketSender.scala 22:19]
  wire [7:0] txq_io_enq_bits_data_3; // @[PacketSender.scala 22:19]
  wire [1:0] txq_io_enq_bits_code_code; // @[PacketSender.scala 22:19]
  wire  txq_io_deq_ready; // @[PacketSender.scala 22:19]
  wire  txq_io_deq_valid; // @[PacketSender.scala 22:19]
  wire [7:0] txq_io_deq_bits_data_0; // @[PacketSender.scala 22:19]
  wire [7:0] txq_io_deq_bits_data_1; // @[PacketSender.scala 22:19]
  wire [7:0] txq_io_deq_bits_data_2; // @[PacketSender.scala 22:19]
  wire [7:0] txq_io_deq_bits_data_3; // @[PacketSender.scala 22:19]
  wire [1:0] txq_io_deq_bits_code_code; // @[PacketSender.scala 22:19]
  reg [15:0] count; // @[PacketSender.scala 23:18]
  reg  state; // @[PacketSender.scala 25:22]
  wire  _T = ~state; // @[Conditional.scala 37:30]
  wire  _GEN_1 = info_io_deq_valid | state; // @[PacketSender.scala 35:32 PacketSender.scala 37:15 PacketSender.scala 25:22]
  wire [16:0] _txq_io_enq_bits_data_0_T = {{1'd0}, count}; // @[PacketSender.scala 45:44]
  wire [15:0] _txq_io_enq_bits_data_1_T_1 = count + 16'h1; // @[PacketSender.scala 45:44]
  wire [15:0] _txq_io_enq_bits_data_2_T_1 = count + 16'h2; // @[PacketSender.scala 45:44]
  wire [15:0] _txq_io_enq_bits_data_3_T_1 = count + 16'h3; // @[PacketSender.scala 45:44]
  wire [15:0] _T_3 = count + 16'h4; // @[PacketSender.scala 47:20]
  wire  _T_4 = _T_3 >= info_io_deq_bits_length; // @[PacketSender.scala 47:33]
  wire [1:0] _GEN_2 = info_io_deq_bits_packetGood ? 2'h2 : 2'h3; // @[PacketSender.scala 52:45 PacketSender.scala 53:39 PacketSender.scala 55:39]
  wire [1:0] _GEN_3 = count == 16'h0 ? 2'h0 : 2'h1; // @[PacketSender.scala 59:31 PacketSender.scala 60:39 PacketSender.scala 62:39]
  wire  _GEN_6 = _T_3 >= info_io_deq_bits_length ? 1'h0 : state; // @[PacketSender.scala 47:61 PacketSender.scala 50:17 PacketSender.scala 25:22]
  wire [1:0] _GEN_7 = _T_3 >= info_io_deq_bits_length ? _GEN_2 : _GEN_3; // @[PacketSender.scala 47:61]
  wire [15:0] _GEN_9 = txq_io_enq_ready ? _txq_io_enq_bits_data_0_T[15:0] : 16'h0; // @[PacketSender.scala 43:30 PacketSender.scala 45:35 PacketSender.scala 31:19]
  wire [15:0] _GEN_10 = txq_io_enq_ready ? _txq_io_enq_bits_data_1_T_1 : 16'h0; // @[PacketSender.scala 43:30 PacketSender.scala 45:35 PacketSender.scala 31:19]
  wire [15:0] _GEN_11 = txq_io_enq_ready ? _txq_io_enq_bits_data_2_T_1 : 16'h0; // @[PacketSender.scala 43:30 PacketSender.scala 45:35 PacketSender.scala 31:19]
  wire [15:0] _GEN_12 = txq_io_enq_ready ? _txq_io_enq_bits_data_3_T_1 : 16'h0; // @[PacketSender.scala 43:30 PacketSender.scala 45:35 PacketSender.scala 31:19]
  wire  _GEN_14 = txq_io_enq_ready & _T_4; // @[PacketSender.scala 43:30 PacketSender.scala 29:21]
  wire [1:0] _GEN_16 = txq_io_enq_ready ? _GEN_7 : 2'h0; // @[PacketSender.scala 43:30 PacketSender.scala 31:19]
  wire [15:0] _GEN_19 = state ? _GEN_9 : 16'h0; // @[Conditional.scala 39:67 PacketSender.scala 31:19]
  wire [15:0] _GEN_20 = state ? _GEN_10 : 16'h0; // @[Conditional.scala 39:67 PacketSender.scala 31:19]
  wire [15:0] _GEN_21 = state ? _GEN_11 : 16'h0; // @[Conditional.scala 39:67 PacketSender.scala 31:19]
  wire [15:0] _GEN_22 = state ? _GEN_12 : 16'h0; // @[Conditional.scala 39:67 PacketSender.scala 31:19]
  wire  _GEN_24 = state & _GEN_14; // @[Conditional.scala 39:67 PacketSender.scala 29:21]
  wire [1:0] _GEN_26 = state ? _GEN_16 : 2'h0; // @[Conditional.scala 39:67 PacketSender.scala 31:19]
  wire [15:0] _GEN_31 = _T ? 16'h0 : _GEN_19; // @[Conditional.scala 40:58 PacketSender.scala 31:19]
  wire [15:0] _GEN_32 = _T ? 16'h0 : _GEN_20; // @[Conditional.scala 40:58 PacketSender.scala 31:19]
  wire [15:0] _GEN_33 = _T ? 16'h0 : _GEN_21; // @[Conditional.scala 40:58 PacketSender.scala 31:19]
  wire [15:0] _GEN_34 = _T ? 16'h0 : _GEN_22; // @[Conditional.scala 40:58 PacketSender.scala 31:19]
  Queue info ( // @[PacketSender.scala 21:20]
    .clock(info_clock),
    .reset(info_reset),
    .io_enq_ready(info_io_enq_ready),
    .io_enq_valid(info_io_enq_valid),
    .io_enq_bits_length(info_io_enq_bits_length),
    .io_enq_bits_packetGood(info_io_enq_bits_packetGood),
    .io_deq_ready(info_io_deq_ready),
    .io_deq_valid(info_io_deq_valid),
    .io_deq_bits_length(info_io_deq_bits_length),
    .io_deq_bits_packetGood(info_io_deq_bits_packetGood)
  );
  Queue_1 txq ( // @[PacketSender.scala 22:19]
    .clock(txq_clock),
    .reset(txq_reset),
    .io_enq_ready(txq_io_enq_ready),
    .io_enq_valid(txq_io_enq_valid),
    .io_enq_bits_data_0(txq_io_enq_bits_data_0),
    .io_enq_bits_data_1(txq_io_enq_bits_data_1),
    .io_enq_bits_data_2(txq_io_enq_bits_data_2),
    .io_enq_bits_data_3(txq_io_enq_bits_data_3),
    .io_enq_bits_code_code(txq_io_enq_bits_code_code),
    .io_deq_ready(txq_io_deq_ready),
    .io_deq_valid(txq_io_deq_valid),
    .io_deq_bits_data_0(txq_io_deq_bits_data_0),
    .io_deq_bits_data_1(txq_io_deq_bits_data_1),
    .io_deq_bits_data_2(txq_io_deq_bits_data_2),
    .io_deq_bits_data_3(txq_io_deq_bits_data_3),
    .io_deq_bits_code_code(txq_io_deq_bits_code_code)
  );
  assign io_packetData_valid = txq_io_deq_valid; // @[PacketSender.scala 27:17]
  assign io_packetData_bits_data_0 = txq_io_deq_bits_data_0; // @[PacketSender.scala 27:17]
  assign io_packetData_bits_data_1 = txq_io_deq_bits_data_1; // @[PacketSender.scala 27:17]
  assign io_packetData_bits_data_2 = txq_io_deq_bits_data_2; // @[PacketSender.scala 27:17]
  assign io_packetData_bits_data_3 = txq_io_deq_bits_data_3; // @[PacketSender.scala 27:17]
  assign io_packetData_bits_code_code = txq_io_deq_bits_code_code; // @[PacketSender.scala 27:17]
  assign io_sendPacket_ready = info_io_enq_ready; // @[PacketSender.scala 26:17]
  assign info_clock = clock;
  assign info_reset = reset;
  assign info_io_enq_valid = io_sendPacket_valid; // @[PacketSender.scala 26:17]
  assign info_io_enq_bits_length = io_sendPacket_bits_length; // @[PacketSender.scala 26:17]
  assign info_io_enq_bits_packetGood = io_sendPacket_bits_packetGood; // @[PacketSender.scala 26:17]
  assign info_io_deq_ready = _T ? 1'h0 : _GEN_24; // @[Conditional.scala 40:58 PacketSender.scala 29:21]
  assign txq_clock = clock;
  assign txq_reset = reset;
  assign txq_io_enq_valid = _T ? 1'h0 : state; // @[Conditional.scala 40:58 PacketSender.scala 30:20]
  assign txq_io_enq_bits_data_0 = _GEN_31[7:0];
  assign txq_io_enq_bits_data_1 = _GEN_32[7:0];
  assign txq_io_enq_bits_data_2 = _GEN_33[7:0];
  assign txq_io_enq_bits_data_3 = _GEN_34[7:0];
  assign txq_io_enq_bits_code_code = _T ? 2'h0 : _GEN_26; // @[Conditional.scala 40:58 PacketSender.scala 31:19]
  assign txq_io_deq_ready = io_packetData_ready; // @[PacketSender.scala 27:17]
  always @(posedge clock) begin
    if (_T) begin // @[Conditional.scala 40:58]
      if (info_io_deq_valid) begin // @[PacketSender.scala 35:32]
        count <= 16'h0; // @[PacketSender.scala 36:15]
      end
    end else if (state) begin // @[Conditional.scala 39:67]
      if (txq_io_enq_ready) begin // @[PacketSender.scala 43:30]
        if (!(_T_3 >= info_io_deq_bits_length)) begin // @[PacketSender.scala 47:61]
          count <= _T_3; // @[PacketSender.scala 64:17]
        end
      end
    end
    if (reset) begin // @[PacketSender.scala 25:22]
      state <= 1'h0; // @[PacketSender.scala 25:22]
    end else if (_T) begin // @[Conditional.scala 40:58]
      state <= _GEN_1;
    end else if (state) begin // @[Conditional.scala 39:67]
      if (txq_io_enq_ready) begin // @[PacketSender.scala 43:30]
        state <= _GEN_6;
      end
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  count = _RAND_0[15:0];
  _RAND_1 = {1{`RANDOM}};
  state = _RAND_1[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module BufferInterfaceStub(
  input        clock,
  input        reset,
  input        io_interface_freeListReq_valid,
  output       io_interface_freeListReq_credit,
  input        io_interface_freeListReq_bits_pool,
  output       io_interface_freeListPage_valid,
  output       io_interface_freeListPage_bits_page_pool,
  output [2:0] io_interface_freeListPage_bits_page_pageNum,
  output       io_interface_writeReqIn_bits_slot,
  input        io_interface_writeReqOut_valid,
  input        io_interface_writeReqOut_bits_slot,
  input        io_interface_writeReqOut_bits_page_pool,
  input  [2:0] io_interface_writeReqOut_bits_page_pageNum,
  input  [3:0] io_interface_writeReqOut_bits_line,
  input  [7:0] io_interface_writeReqOut_bits_data_0,
  input  [7:0] io_interface_writeReqOut_bits_data_1,
  input  [7:0] io_interface_writeReqOut_bits_data_2,
  input  [7:0] io_interface_writeReqOut_bits_data_3,
  output       io_writeReqOut_valid,
  output       io_writeReqOut_bits_slot,
  output       io_writeReqOut_bits_page_pool,
  output [2:0] io_writeReqOut_bits_page_pageNum,
  output [3:0] io_writeReqOut_bits_line,
  output [7:0] io_writeReqOut_bits_data_0,
  output [7:0] io_writeReqOut_bits_data_1,
  output [7:0] io_writeReqOut_bits_data_2,
  output [7:0] io_writeReqOut_bits_data_3
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_4;
`endif // RANDOMIZE_REG_INIT
  reg  io_interface_freeListReq_credit_REG; // @[PacketWriterTester.scala 32:45]
  reg  io_interface_freeListPage_valid_REG; // @[PacketWriterTester.scala 34:45]
  reg [2:0] pageNum; // @[PacketWriterTester.scala 44:24]
  reg  io_interface_freeListPage_bits_page_pool_REG; // @[PacketWriterTester.scala 47:54]
  wire [2:0] _pageNum_T_1 = pageNum + 3'h1; // @[PacketWriterTester.scala 49:24]
  reg  slotCount; // @[PacketWriterTester.scala 53:26]
  assign io_interface_freeListReq_credit = io_interface_freeListReq_credit_REG; // @[PacketWriterTester.scala 32:35]
  assign io_interface_freeListPage_valid = io_interface_freeListPage_valid_REG; // @[PacketWriterTester.scala 34:35]
  assign io_interface_freeListPage_bits_page_pool = io_interface_freeListPage_bits_page_pool_REG; // @[PacketWriterTester.scala 47:44]
  assign io_interface_freeListPage_bits_page_pageNum = pageNum; // @[PacketWriterTester.scala 46:47]
  assign io_interface_writeReqIn_bits_slot = slotCount; // @[PacketWriterTester.scala 61:37]
  assign io_writeReqOut_valid = io_interface_writeReqOut_valid; // @[PacketWriterTester.scala 64:18]
  assign io_writeReqOut_bits_slot = io_interface_writeReqOut_bits_slot; // @[PacketWriterTester.scala 64:18]
  assign io_writeReqOut_bits_page_pool = io_interface_writeReqOut_bits_page_pool; // @[PacketWriterTester.scala 64:18]
  assign io_writeReqOut_bits_page_pageNum = io_interface_writeReqOut_bits_page_pageNum; // @[PacketWriterTester.scala 64:18]
  assign io_writeReqOut_bits_line = io_interface_writeReqOut_bits_line; // @[PacketWriterTester.scala 64:18]
  assign io_writeReqOut_bits_data_0 = io_interface_writeReqOut_bits_data_0; // @[PacketWriterTester.scala 64:18]
  assign io_writeReqOut_bits_data_1 = io_interface_writeReqOut_bits_data_1; // @[PacketWriterTester.scala 64:18]
  assign io_writeReqOut_bits_data_2 = io_interface_writeReqOut_bits_data_2; // @[PacketWriterTester.scala 64:18]
  assign io_writeReqOut_bits_data_3 = io_interface_writeReqOut_bits_data_3; // @[PacketWriterTester.scala 64:18]
  always @(posedge clock) begin
    if (reset) begin // @[PacketWriterTester.scala 32:45]
      io_interface_freeListReq_credit_REG <= 1'h0; // @[PacketWriterTester.scala 32:45]
    end else begin
      io_interface_freeListReq_credit_REG <= io_interface_freeListReq_valid; // @[PacketWriterTester.scala 32:45]
    end
    if (reset) begin // @[PacketWriterTester.scala 34:45]
      io_interface_freeListPage_valid_REG <= 1'h0; // @[PacketWriterTester.scala 34:45]
    end else begin
      io_interface_freeListPage_valid_REG <= io_interface_freeListReq_valid; // @[PacketWriterTester.scala 34:45]
    end
    if (reset) begin // @[PacketWriterTester.scala 44:24]
      pageNum <= 3'h0; // @[PacketWriterTester.scala 44:24]
    end else if (io_interface_freeListPage_valid) begin // @[PacketWriterTester.scala 48:42]
      pageNum <= _pageNum_T_1; // @[PacketWriterTester.scala 49:13]
    end
    io_interface_freeListPage_bits_page_pool_REG <= io_interface_freeListReq_bits_pool; // @[PacketWriterTester.scala 47:54]
    if (reset) begin // @[PacketWriterTester.scala 53:26]
      slotCount <= 1'h0; // @[PacketWriterTester.scala 53:26]
    end else if (slotCount) begin // @[PacketWriterTester.scala 54:45]
      slotCount <= 1'h0; // @[PacketWriterTester.scala 55:15]
    end else begin
      slotCount <= slotCount + 1'h1; // @[PacketWriterTester.scala 57:15]
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  io_interface_freeListReq_credit_REG = _RAND_0[0:0];
  _RAND_1 = {1{`RANDOM}};
  io_interface_freeListPage_valid_REG = _RAND_1[0:0];
  _RAND_2 = {1{`RANDOM}};
  pageNum = _RAND_2[2:0];
  _RAND_3 = {1{`RANDOM}};
  io_interface_freeListPage_bits_page_pool_REG = _RAND_3[0:0];
  _RAND_4 = {1{`RANDOM}};
  slotCount = _RAND_4[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module DCCreditSender_2(
  input   clock,
  input   reset,
  output  io_enq_ready,
  input   io_enq_valid,
  input   io_enq_bits_pool,
  output  io_deq_valid,
  input   io_deq_credit,
  output  io_deq_bits_pool
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
`endif // RANDOMIZE_REG_INIT
  reg  icredit; // @[DCCredit.scala 28:24]
  reg [1:0] curCredit; // @[DCCredit.scala 29:26]
  wire [1:0] _curCredit_T_1 = curCredit + 2'h1; // @[DCCredit.scala 31:28]
  wire [1:0] _curCredit_T_3 = curCredit - 2'h1; // @[DCCredit.scala 33:28]
  reg  dataOut_pool; // @[Reg.scala 15:16]
  reg  validOut; // @[DCCredit.scala 37:25]
  assign io_enq_ready = curCredit > 2'h0; // @[DCCredit.scala 35:29]
  assign io_deq_valid = validOut; // @[DCCredit.scala 38:16]
  assign io_deq_bits_pool = dataOut_pool; // @[DCCredit.scala 39:15]
  always @(posedge clock) begin
    icredit <= io_deq_credit; // @[DCCredit.scala 28:24]
    if (reset) begin // @[DCCredit.scala 29:26]
      curCredit <= 2'h2; // @[DCCredit.scala 29:26]
    end else if (icredit & ~io_enq_ready) begin // @[DCCredit.scala 30:36]
      curCredit <= _curCredit_T_1; // @[DCCredit.scala 31:15]
    end else if (~icredit & io_enq_ready) begin // @[DCCredit.scala 32:41]
      curCredit <= _curCredit_T_3; // @[DCCredit.scala 33:15]
    end
    if (io_enq_ready) begin // @[Reg.scala 16:19]
      dataOut_pool <= io_enq_bits_pool; // @[Reg.scala 16:23]
    end
    if (reset) begin // @[DCCredit.scala 37:25]
      validOut <= 1'h0; // @[DCCredit.scala 37:25]
    end else begin
      validOut <= io_enq_ready; // @[DCCredit.scala 37:25]
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  icredit = _RAND_0[0:0];
  _RAND_1 = {1{`RANDOM}};
  curCredit = _RAND_1[1:0];
  _RAND_2 = {1{`RANDOM}};
  dataOut_pool = _RAND_2[0:0];
  _RAND_3 = {1{`RANDOM}};
  validOut = _RAND_3[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module Queue_2(
  input        clock,
  input        reset,
  output       io_enq_ready,
  input        io_enq_valid,
  input        io_enq_bits_page_pool,
  input  [2:0] io_enq_bits_page_pageNum,
  input        io_deq_ready,
  output       io_deq_valid,
  output       io_deq_bits_page_pool,
  output [2:0] io_deq_bits_page_pageNum
);
`ifdef RANDOMIZE_MEM_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_4;
`endif // RANDOMIZE_REG_INIT
  reg  ram_page_pool [0:1]; // @[Decoupled.scala 218:16]
  wire  ram_page_pool_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_page_pool_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_page_pool_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_page_pool_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_page_pool_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_page_pool_MPORT_en; // @[Decoupled.scala 218:16]
  reg [2:0] ram_page_pageNum [0:1]; // @[Decoupled.scala 218:16]
  wire [2:0] ram_page_pageNum_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_page_pageNum_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [2:0] ram_page_pageNum_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_page_pageNum_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_page_pageNum_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_page_pageNum_MPORT_en; // @[Decoupled.scala 218:16]
  reg  enq_ptr_value; // @[Counter.scala 60:40]
  reg  deq_ptr_value; // @[Counter.scala 60:40]
  reg  maybe_full; // @[Decoupled.scala 221:27]
  wire  ptr_match = enq_ptr_value == deq_ptr_value; // @[Decoupled.scala 223:33]
  wire  empty = ptr_match & ~maybe_full; // @[Decoupled.scala 224:25]
  wire  full = ptr_match & maybe_full; // @[Decoupled.scala 225:24]
  wire  do_enq = io_enq_ready & io_enq_valid; // @[Decoupled.scala 40:37]
  wire  do_deq = io_deq_ready & io_deq_valid; // @[Decoupled.scala 40:37]
  assign ram_page_pool_io_deq_bits_MPORT_addr = deq_ptr_value;
  assign ram_page_pool_io_deq_bits_MPORT_data = ram_page_pool[ram_page_pool_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_page_pool_MPORT_data = io_enq_bits_page_pool;
  assign ram_page_pool_MPORT_addr = enq_ptr_value;
  assign ram_page_pool_MPORT_mask = 1'h1;
  assign ram_page_pool_MPORT_en = io_enq_ready & io_enq_valid;
  assign ram_page_pageNum_io_deq_bits_MPORT_addr = deq_ptr_value;
  assign ram_page_pageNum_io_deq_bits_MPORT_data = ram_page_pageNum[ram_page_pageNum_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_page_pageNum_MPORT_data = io_enq_bits_page_pageNum;
  assign ram_page_pageNum_MPORT_addr = enq_ptr_value;
  assign ram_page_pageNum_MPORT_mask = 1'h1;
  assign ram_page_pageNum_MPORT_en = io_enq_ready & io_enq_valid;
  assign io_enq_ready = ~full; // @[Decoupled.scala 241:19]
  assign io_deq_valid = ~empty; // @[Decoupled.scala 240:19]
  assign io_deq_bits_page_pool = ram_page_pool_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  assign io_deq_bits_page_pageNum = ram_page_pageNum_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  always @(posedge clock) begin
    if(ram_page_pool_MPORT_en & ram_page_pool_MPORT_mask) begin
      ram_page_pool[ram_page_pool_MPORT_addr] <= ram_page_pool_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if(ram_page_pageNum_MPORT_en & ram_page_pageNum_MPORT_mask) begin
      ram_page_pageNum[ram_page_pageNum_MPORT_addr] <= ram_page_pageNum_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if (reset) begin // @[Counter.scala 60:40]
      enq_ptr_value <= 1'h0; // @[Counter.scala 60:40]
    end else if (do_enq) begin // @[Decoupled.scala 229:17]
      enq_ptr_value <= enq_ptr_value + 1'h1; // @[Counter.scala 76:15]
    end
    if (reset) begin // @[Counter.scala 60:40]
      deq_ptr_value <= 1'h0; // @[Counter.scala 60:40]
    end else if (do_deq) begin // @[Decoupled.scala 233:17]
      deq_ptr_value <= deq_ptr_value + 1'h1; // @[Counter.scala 76:15]
    end
    if (reset) begin // @[Decoupled.scala 221:27]
      maybe_full <= 1'h0; // @[Decoupled.scala 221:27]
    end else if (do_enq != do_deq) begin // @[Decoupled.scala 236:28]
      maybe_full <= do_enq; // @[Decoupled.scala 237:16]
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_MEM_INIT
  _RAND_0 = {1{`RANDOM}};
  for (initvar = 0; initvar < 2; initvar = initvar+1)
    ram_page_pool[initvar] = _RAND_0[0:0];
  _RAND_1 = {1{`RANDOM}};
  for (initvar = 0; initvar < 2; initvar = initvar+1)
    ram_page_pageNum[initvar] = _RAND_1[2:0];
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  _RAND_2 = {1{`RANDOM}};
  enq_ptr_value = _RAND_2[0:0];
  _RAND_3 = {1{`RANDOM}};
  deq_ptr_value = _RAND_3[0:0];
  _RAND_4 = {1{`RANDOM}};
  maybe_full = _RAND_4[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module DCCreditReceiver(
  input        clock,
  input        reset,
  input        io_enq_valid,
  input        io_enq_bits_page_pool,
  input  [2:0] io_enq_bits_page_pageNum,
  input        io_deq_ready,
  output       io_deq_valid,
  output       io_deq_bits_page_pool,
  output [2:0] io_deq_bits_page_pageNum
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
`endif // RANDOMIZE_REG_INIT
  wire  outFifo_clock; // @[DCCredit.scala 53:23]
  wire  outFifo_reset; // @[DCCredit.scala 53:23]
  wire  outFifo_io_enq_ready; // @[DCCredit.scala 53:23]
  wire  outFifo_io_enq_valid; // @[DCCredit.scala 53:23]
  wire  outFifo_io_enq_bits_page_pool; // @[DCCredit.scala 53:23]
  wire [2:0] outFifo_io_enq_bits_page_pageNum; // @[DCCredit.scala 53:23]
  wire  outFifo_io_deq_ready; // @[DCCredit.scala 53:23]
  wire  outFifo_io_deq_valid; // @[DCCredit.scala 53:23]
  wire  outFifo_io_deq_bits_page_pool; // @[DCCredit.scala 53:23]
  wire [2:0] outFifo_io_deq_bits_page_pageNum; // @[DCCredit.scala 53:23]
  reg  ivalid; // @[DCCredit.scala 51:23]
  reg  idata_page_pool; // @[DCCredit.scala 52:22]
  reg [2:0] idata_page_pageNum; // @[DCCredit.scala 52:22]
  Queue_2 outFifo ( // @[DCCredit.scala 53:23]
    .clock(outFifo_clock),
    .reset(outFifo_reset),
    .io_enq_ready(outFifo_io_enq_ready),
    .io_enq_valid(outFifo_io_enq_valid),
    .io_enq_bits_page_pool(outFifo_io_enq_bits_page_pool),
    .io_enq_bits_page_pageNum(outFifo_io_enq_bits_page_pageNum),
    .io_deq_ready(outFifo_io_deq_ready),
    .io_deq_valid(outFifo_io_deq_valid),
    .io_deq_bits_page_pool(outFifo_io_deq_bits_page_pool),
    .io_deq_bits_page_pageNum(outFifo_io_deq_bits_page_pageNum)
  );
  assign io_deq_valid = outFifo_io_deq_valid; // @[DCCredit.scala 57:10]
  assign io_deq_bits_page_pool = outFifo_io_deq_bits_page_pool; // @[DCCredit.scala 57:10]
  assign io_deq_bits_page_pageNum = outFifo_io_deq_bits_page_pageNum; // @[DCCredit.scala 57:10]
  assign outFifo_clock = clock;
  assign outFifo_reset = reset;
  assign outFifo_io_enq_valid = ivalid; // @[DCCredit.scala 54:24]
  assign outFifo_io_enq_bits_page_pool = idata_page_pool; // @[DCCredit.scala 55:23]
  assign outFifo_io_enq_bits_page_pageNum = idata_page_pageNum; // @[DCCredit.scala 55:23]
  assign outFifo_io_deq_ready = io_deq_ready; // @[DCCredit.scala 57:10]
  always @(posedge clock) begin
    ivalid <= io_enq_valid; // @[DCCredit.scala 51:23]
    idata_page_pool <= io_enq_bits_page_pool; // @[DCCredit.scala 52:22]
    idata_page_pageNum <= io_enq_bits_page_pageNum; // @[DCCredit.scala 52:22]
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  ivalid = _RAND_0[0:0];
  _RAND_1 = {1{`RANDOM}};
  idata_page_pool = _RAND_1[0:0];
  _RAND_2 = {1{`RANDOM}};
  idata_page_pageNum = _RAND_2[2:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module BasicPacketBufferAllocator(
  input        clock,
  input        reset,
  output       io_freeListReq_valid,
  input        io_freeListReq_credit,
  output       io_freeListReq_bits_pool,
  input        io_freeListPage_valid,
  input        io_freeListPage_bits_page_pool,
  input  [2:0] io_freeListPage_bits_page_pageNum,
  input        io_freePage_ready,
  output       io_freePage_valid,
  output       io_freePage_bits_pool,
  output [2:0] io_freePage_bits_pageNum
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
`endif // RANDOMIZE_REG_INIT
  wire  freeReqSender_clock; // @[PacketWriter.scala 150:29]
  wire  freeReqSender_reset; // @[PacketWriter.scala 150:29]
  wire  freeReqSender_io_enq_ready; // @[PacketWriter.scala 150:29]
  wire  freeReqSender_io_enq_valid; // @[PacketWriter.scala 150:29]
  wire  freeReqSender_io_enq_bits_pool; // @[PacketWriter.scala 150:29]
  wire  freeReqSender_io_deq_valid; // @[PacketWriter.scala 150:29]
  wire  freeReqSender_io_deq_credit; // @[PacketWriter.scala 150:29]
  wire  freeReqSender_io_deq_bits_pool; // @[PacketWriter.scala 150:29]
  wire  freeRespRx_clock; // @[PacketWriter.scala 151:26]
  wire  freeRespRx_reset; // @[PacketWriter.scala 151:26]
  wire  freeRespRx_io_enq_valid; // @[PacketWriter.scala 151:26]
  wire  freeRespRx_io_enq_bits_page_pool; // @[PacketWriter.scala 151:26]
  wire [2:0] freeRespRx_io_enq_bits_page_pageNum; // @[PacketWriter.scala 151:26]
  wire  freeRespRx_io_deq_ready; // @[PacketWriter.scala 151:26]
  wire  freeRespRx_io_deq_valid; // @[PacketWriter.scala 151:26]
  wire  freeRespRx_io_deq_bits_page_pool; // @[PacketWriter.scala 151:26]
  wire [2:0] freeRespRx_io_deq_bits_page_pageNum; // @[PacketWriter.scala 151:26]
  reg  REG; // @[PacketWriter.scala 169:26]
  DCCreditSender_2 freeReqSender ( // @[PacketWriter.scala 150:29]
    .clock(freeReqSender_clock),
    .reset(freeReqSender_reset),
    .io_enq_ready(freeReqSender_io_enq_ready),
    .io_enq_valid(freeReqSender_io_enq_valid),
    .io_enq_bits_pool(freeReqSender_io_enq_bits_pool),
    .io_deq_valid(freeReqSender_io_deq_valid),
    .io_deq_credit(freeReqSender_io_deq_credit),
    .io_deq_bits_pool(freeReqSender_io_deq_bits_pool)
  );
  DCCreditReceiver freeRespRx ( // @[PacketWriter.scala 151:26]
    .clock(freeRespRx_clock),
    .reset(freeRespRx_reset),
    .io_enq_valid(freeRespRx_io_enq_valid),
    .io_enq_bits_page_pool(freeRespRx_io_enq_bits_page_pool),
    .io_enq_bits_page_pageNum(freeRespRx_io_enq_bits_page_pageNum),
    .io_deq_ready(freeRespRx_io_deq_ready),
    .io_deq_valid(freeRespRx_io_deq_valid),
    .io_deq_bits_page_pool(freeRespRx_io_deq_bits_page_pool),
    .io_deq_bits_page_pageNum(freeRespRx_io_deq_bits_page_pageNum)
  );
  assign io_freeListReq_valid = freeReqSender_io_deq_valid; // @[PacketWriter.scala 154:18]
  assign io_freeListReq_bits_pool = freeReqSender_io_deq_bits_pool; // @[PacketWriter.scala 154:18]
  assign io_freePage_valid = freeRespRx_io_deq_valid; // @[PacketWriter.scala 158:21]
  assign io_freePage_bits_pool = freeRespRx_io_deq_bits_page_pool; // @[PacketWriter.scala 160:20]
  assign io_freePage_bits_pageNum = freeRespRx_io_deq_bits_page_pageNum; // @[PacketWriter.scala 160:20]
  assign freeReqSender_clock = clock;
  assign freeReqSender_reset = reset;
  assign freeReqSender_io_enq_valid = 1'h1; // @[PacketWriter.scala 163:50]
  assign freeReqSender_io_enq_bits_pool = REG; // @[PacketWriter.scala 170:36]
  assign freeReqSender_io_deq_credit = io_freeListReq_credit; // @[PacketWriter.scala 154:18]
  assign freeRespRx_clock = clock;
  assign freeRespRx_reset = reset;
  assign freeRespRx_io_enq_valid = io_freeListPage_valid; // @[PacketWriter.scala 155:21]
  assign freeRespRx_io_enq_bits_page_pool = io_freeListPage_bits_page_pool; // @[PacketWriter.scala 155:21]
  assign freeRespRx_io_enq_bits_page_pageNum = io_freeListPage_bits_page_pageNum; // @[PacketWriter.scala 155:21]
  assign freeRespRx_io_deq_ready = io_freePage_ready; // @[PacketWriter.scala 159:27]
  always @(posedge clock) begin
    if (reset) begin // @[PacketWriter.scala 169:26]
      REG <= 1'h0; // @[PacketWriter.scala 169:26]
    end else if (freeReqSender_io_enq_valid) begin // @[PacketWriter.scala 171:39]
      if (REG) begin // @[PacketWriter.scala 172:43]
        REG <= 1'h0; // @[PacketWriter.scala 173:17]
      end else begin
        REG <= REG + 1'h1; // @[PacketWriter.scala 175:17]
      end
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  REG = _RAND_0[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module Queue_3(
  input        clock,
  input        reset,
  output       io_enq_ready,
  input        io_enq_valid,
  input        io_enq_bits_page_pool,
  input  [2:0] io_enq_bits_page_pageNum,
  input        io_deq_ready,
  output       io_deq_valid,
  output       io_deq_bits_page_pool,
  output [2:0] io_deq_bits_page_pageNum,
  output [3:0] io_deq_bits_line
);
`ifdef RANDOMIZE_MEM_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_3;
`endif // RANDOMIZE_REG_INIT
  reg  ram_page_pool [0:0]; // @[Decoupled.scala 218:16]
  wire  ram_page_pool_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_page_pool_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_page_pool_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_page_pool_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_page_pool_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_page_pool_MPORT_en; // @[Decoupled.scala 218:16]
  reg [2:0] ram_page_pageNum [0:0]; // @[Decoupled.scala 218:16]
  wire [2:0] ram_page_pageNum_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_page_pageNum_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [2:0] ram_page_pageNum_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_page_pageNum_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_page_pageNum_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_page_pageNum_MPORT_en; // @[Decoupled.scala 218:16]
  reg [3:0] ram_line [0:0]; // @[Decoupled.scala 218:16]
  wire [3:0] ram_line_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_line_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [3:0] ram_line_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_line_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_line_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_line_MPORT_en; // @[Decoupled.scala 218:16]
  reg  maybe_full; // @[Decoupled.scala 221:27]
  wire  empty = ~maybe_full; // @[Decoupled.scala 224:28]
  wire  do_enq = io_enq_ready & io_enq_valid; // @[Decoupled.scala 40:37]
  wire  do_deq = io_deq_ready & io_deq_valid; // @[Decoupled.scala 40:37]
  assign ram_page_pool_io_deq_bits_MPORT_addr = 1'h0;
  assign ram_page_pool_io_deq_bits_MPORT_data = ram_page_pool[ram_page_pool_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_page_pool_MPORT_data = io_enq_bits_page_pool;
  assign ram_page_pool_MPORT_addr = 1'h0;
  assign ram_page_pool_MPORT_mask = 1'h1;
  assign ram_page_pool_MPORT_en = io_enq_ready & io_enq_valid;
  assign ram_page_pageNum_io_deq_bits_MPORT_addr = 1'h0;
  assign ram_page_pageNum_io_deq_bits_MPORT_data = ram_page_pageNum[ram_page_pageNum_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_page_pageNum_MPORT_data = io_enq_bits_page_pageNum;
  assign ram_page_pageNum_MPORT_addr = 1'h0;
  assign ram_page_pageNum_MPORT_mask = 1'h1;
  assign ram_page_pageNum_MPORT_en = io_enq_ready & io_enq_valid;
  assign ram_line_io_deq_bits_MPORT_addr = 1'h0;
  assign ram_line_io_deq_bits_MPORT_data = ram_line[ram_line_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_line_MPORT_data = 4'h0;
  assign ram_line_MPORT_addr = 1'h0;
  assign ram_line_MPORT_mask = 1'h1;
  assign ram_line_MPORT_en = io_enq_ready & io_enq_valid;
  assign io_enq_ready = ~maybe_full; // @[Decoupled.scala 241:19]
  assign io_deq_valid = ~empty; // @[Decoupled.scala 240:19]
  assign io_deq_bits_page_pool = ram_page_pool_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  assign io_deq_bits_page_pageNum = ram_page_pageNum_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  assign io_deq_bits_line = ram_line_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  always @(posedge clock) begin
    if(ram_page_pool_MPORT_en & ram_page_pool_MPORT_mask) begin
      ram_page_pool[ram_page_pool_MPORT_addr] <= ram_page_pool_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if(ram_page_pageNum_MPORT_en & ram_page_pageNum_MPORT_mask) begin
      ram_page_pageNum[ram_page_pageNum_MPORT_addr] <= ram_page_pageNum_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if(ram_line_MPORT_en & ram_line_MPORT_mask) begin
      ram_line[ram_line_MPORT_addr] <= ram_line_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if (reset) begin // @[Decoupled.scala 221:27]
      maybe_full <= 1'h0; // @[Decoupled.scala 221:27]
    end else if (do_enq != do_deq) begin // @[Decoupled.scala 236:28]
      maybe_full <= do_enq; // @[Decoupled.scala 237:16]
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_MEM_INIT
  _RAND_0 = {1{`RANDOM}};
  for (initvar = 0; initvar < 1; initvar = initvar+1)
    ram_page_pool[initvar] = _RAND_0[0:0];
  _RAND_1 = {1{`RANDOM}};
  for (initvar = 0; initvar < 1; initvar = initvar+1)
    ram_page_pageNum[initvar] = _RAND_1[2:0];
  _RAND_2 = {1{`RANDOM}};
  for (initvar = 0; initvar < 1; initvar = initvar+1)
    ram_line[initvar] = _RAND_2[3:0];
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  _RAND_3 = {1{`RANDOM}};
  maybe_full = _RAND_3[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module Queue_4(
  input        clock,
  input        reset,
  output       io_enq_ready,
  input        io_enq_valid,
  input  [7:0] io_enq_bits_data_0,
  input  [7:0] io_enq_bits_data_1,
  input  [7:0] io_enq_bits_data_2,
  input  [7:0] io_enq_bits_data_3,
  input        io_deq_ready,
  output       io_deq_valid,
  output [7:0] io_deq_bits_data_0,
  output [7:0] io_deq_bits_data_1,
  output [7:0] io_deq_bits_data_2,
  output [7:0] io_deq_bits_data_3
);
`ifdef RANDOMIZE_MEM_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_4;
`endif // RANDOMIZE_REG_INIT
  reg [7:0] ram_data_0 [0:0]; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_0_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_0_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_0_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_0_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_data_0_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_data_0_MPORT_en; // @[Decoupled.scala 218:16]
  reg [7:0] ram_data_1 [0:0]; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_1_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_1_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_1_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_1_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_data_1_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_data_1_MPORT_en; // @[Decoupled.scala 218:16]
  reg [7:0] ram_data_2 [0:0]; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_2_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_2_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_2_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_2_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_data_2_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_data_2_MPORT_en; // @[Decoupled.scala 218:16]
  reg [7:0] ram_data_3 [0:0]; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_3_io_deq_bits_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_3_io_deq_bits_MPORT_addr; // @[Decoupled.scala 218:16]
  wire [7:0] ram_data_3_MPORT_data; // @[Decoupled.scala 218:16]
  wire  ram_data_3_MPORT_addr; // @[Decoupled.scala 218:16]
  wire  ram_data_3_MPORT_mask; // @[Decoupled.scala 218:16]
  wire  ram_data_3_MPORT_en; // @[Decoupled.scala 218:16]
  reg  maybe_full; // @[Decoupled.scala 221:27]
  wire  empty = ~maybe_full; // @[Decoupled.scala 224:28]
  wire  do_enq = io_enq_ready & io_enq_valid; // @[Decoupled.scala 40:37]
  wire  do_deq = io_deq_ready & io_deq_valid; // @[Decoupled.scala 40:37]
  assign ram_data_0_io_deq_bits_MPORT_addr = 1'h0;
  assign ram_data_0_io_deq_bits_MPORT_data = ram_data_0[ram_data_0_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_data_0_MPORT_data = io_enq_bits_data_0;
  assign ram_data_0_MPORT_addr = 1'h0;
  assign ram_data_0_MPORT_mask = 1'h1;
  assign ram_data_0_MPORT_en = io_enq_ready & io_enq_valid;
  assign ram_data_1_io_deq_bits_MPORT_addr = 1'h0;
  assign ram_data_1_io_deq_bits_MPORT_data = ram_data_1[ram_data_1_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_data_1_MPORT_data = io_enq_bits_data_1;
  assign ram_data_1_MPORT_addr = 1'h0;
  assign ram_data_1_MPORT_mask = 1'h1;
  assign ram_data_1_MPORT_en = io_enq_ready & io_enq_valid;
  assign ram_data_2_io_deq_bits_MPORT_addr = 1'h0;
  assign ram_data_2_io_deq_bits_MPORT_data = ram_data_2[ram_data_2_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_data_2_MPORT_data = io_enq_bits_data_2;
  assign ram_data_2_MPORT_addr = 1'h0;
  assign ram_data_2_MPORT_mask = 1'h1;
  assign ram_data_2_MPORT_en = io_enq_ready & io_enq_valid;
  assign ram_data_3_io_deq_bits_MPORT_addr = 1'h0;
  assign ram_data_3_io_deq_bits_MPORT_data = ram_data_3[ram_data_3_io_deq_bits_MPORT_addr]; // @[Decoupled.scala 218:16]
  assign ram_data_3_MPORT_data = io_enq_bits_data_3;
  assign ram_data_3_MPORT_addr = 1'h0;
  assign ram_data_3_MPORT_mask = 1'h1;
  assign ram_data_3_MPORT_en = io_enq_ready & io_enq_valid;
  assign io_enq_ready = ~maybe_full; // @[Decoupled.scala 241:19]
  assign io_deq_valid = ~empty; // @[Decoupled.scala 240:19]
  assign io_deq_bits_data_0 = ram_data_0_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  assign io_deq_bits_data_1 = ram_data_1_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  assign io_deq_bits_data_2 = ram_data_2_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  assign io_deq_bits_data_3 = ram_data_3_io_deq_bits_MPORT_data; // @[Decoupled.scala 242:15]
  always @(posedge clock) begin
    if(ram_data_0_MPORT_en & ram_data_0_MPORT_mask) begin
      ram_data_0[ram_data_0_MPORT_addr] <= ram_data_0_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if(ram_data_1_MPORT_en & ram_data_1_MPORT_mask) begin
      ram_data_1[ram_data_1_MPORT_addr] <= ram_data_1_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if(ram_data_2_MPORT_en & ram_data_2_MPORT_mask) begin
      ram_data_2[ram_data_2_MPORT_addr] <= ram_data_2_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if(ram_data_3_MPORT_en & ram_data_3_MPORT_mask) begin
      ram_data_3[ram_data_3_MPORT_addr] <= ram_data_3_MPORT_data; // @[Decoupled.scala 218:16]
    end
    if (reset) begin // @[Decoupled.scala 221:27]
      maybe_full <= 1'h0; // @[Decoupled.scala 221:27]
    end else if (do_enq != do_deq) begin // @[Decoupled.scala 236:28]
      maybe_full <= do_enq; // @[Decoupled.scala 237:16]
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_MEM_INIT
  _RAND_0 = {1{`RANDOM}};
  for (initvar = 0; initvar < 1; initvar = initvar+1)
    ram_data_0[initvar] = _RAND_0[7:0];
  _RAND_1 = {1{`RANDOM}};
  for (initvar = 0; initvar < 1; initvar = initvar+1)
    ram_data_1[initvar] = _RAND_1[7:0];
  _RAND_2 = {1{`RANDOM}};
  for (initvar = 0; initvar < 1; initvar = initvar+1)
    ram_data_2[initvar] = _RAND_2[7:0];
  _RAND_3 = {1{`RANDOM}};
  for (initvar = 0; initvar < 1; initvar = initvar+1)
    ram_data_3[initvar] = _RAND_3[7:0];
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  _RAND_4 = {1{`RANDOM}};
  maybe_full = _RAND_4[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module PacketWriter(
  input        clock,
  input        reset,
  output       io_portDataIn_ready,
  input        io_portDataIn_valid,
  input  [7:0] io_portDataIn_bits_data_0,
  input  [7:0] io_portDataIn_bits_data_1,
  input  [7:0] io_portDataIn_bits_data_2,
  input  [7:0] io_portDataIn_bits_data_3,
  input  [1:0] io_portDataIn_bits_code_code,
  output       io_interface_freeListReq_valid,
  input        io_interface_freeListReq_credit,
  output       io_interface_freeListReq_bits_pool,
  input        io_interface_freeListPage_valid,
  input        io_interface_freeListPage_bits_page_pool,
  input  [2:0] io_interface_freeListPage_bits_page_pageNum,
  input        io_interface_writeReqIn_bits_slot,
  output       io_interface_writeReqOut_valid,
  output       io_interface_writeReqOut_bits_slot,
  output       io_interface_writeReqOut_bits_page_pool,
  output [2:0] io_interface_writeReqOut_bits_page_pageNum,
  output [3:0] io_interface_writeReqOut_bits_line,
  output [7:0] io_interface_writeReqOut_bits_data_0,
  output [7:0] io_interface_writeReqOut_bits_data_1,
  output [7:0] io_interface_writeReqOut_bits_data_2,
  output [7:0] io_interface_writeReqOut_bits_data_3
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_4;
  reg [31:0] _RAND_5;
  reg [31:0] _RAND_6;
  reg [31:0] _RAND_7;
  reg [31:0] _RAND_8;
  reg [31:0] _RAND_9;
  reg [31:0] _RAND_10;
  reg [31:0] _RAND_11;
  reg [31:0] _RAND_12;
`endif // RANDOMIZE_REG_INIT
  wire  bufferAllocator_clock; // @[PacketWriter.scala 34:31]
  wire  bufferAllocator_reset; // @[PacketWriter.scala 34:31]
  wire  bufferAllocator_io_freeListReq_valid; // @[PacketWriter.scala 34:31]
  wire  bufferAllocator_io_freeListReq_credit; // @[PacketWriter.scala 34:31]
  wire  bufferAllocator_io_freeListReq_bits_pool; // @[PacketWriter.scala 34:31]
  wire  bufferAllocator_io_freeListPage_valid; // @[PacketWriter.scala 34:31]
  wire  bufferAllocator_io_freeListPage_bits_page_pool; // @[PacketWriter.scala 34:31]
  wire [2:0] bufferAllocator_io_freeListPage_bits_page_pageNum; // @[PacketWriter.scala 34:31]
  wire  bufferAllocator_io_freePage_ready; // @[PacketWriter.scala 34:31]
  wire  bufferAllocator_io_freePage_valid; // @[PacketWriter.scala 34:31]
  wire  bufferAllocator_io_freePage_bits_pool; // @[PacketWriter.scala 34:31]
  wire [2:0] bufferAllocator_io_freePage_bits_pageNum; // @[PacketWriter.scala 34:31]
  wire  lineInfoHold_clock; // @[PacketWriter.scala 36:28]
  wire  lineInfoHold_reset; // @[PacketWriter.scala 36:28]
  wire  lineInfoHold_io_enq_ready; // @[PacketWriter.scala 36:28]
  wire  lineInfoHold_io_enq_valid; // @[PacketWriter.scala 36:28]
  wire  lineInfoHold_io_enq_bits_page_pool; // @[PacketWriter.scala 36:28]
  wire [2:0] lineInfoHold_io_enq_bits_page_pageNum; // @[PacketWriter.scala 36:28]
  wire  lineInfoHold_io_deq_ready; // @[PacketWriter.scala 36:28]
  wire  lineInfoHold_io_deq_valid; // @[PacketWriter.scala 36:28]
  wire  lineInfoHold_io_deq_bits_page_pool; // @[PacketWriter.scala 36:28]
  wire [2:0] lineInfoHold_io_deq_bits_page_pageNum; // @[PacketWriter.scala 36:28]
  wire [3:0] lineInfoHold_io_deq_bits_line; // @[PacketWriter.scala 36:28]
  wire  dataQ_clock; // @[PacketWriter.scala 39:21]
  wire  dataQ_reset; // @[PacketWriter.scala 39:21]
  wire  dataQ_io_enq_ready; // @[PacketWriter.scala 39:21]
  wire  dataQ_io_enq_valid; // @[PacketWriter.scala 39:21]
  wire [7:0] dataQ_io_enq_bits_data_0; // @[PacketWriter.scala 39:21]
  wire [7:0] dataQ_io_enq_bits_data_1; // @[PacketWriter.scala 39:21]
  wire [7:0] dataQ_io_enq_bits_data_2; // @[PacketWriter.scala 39:21]
  wire [7:0] dataQ_io_enq_bits_data_3; // @[PacketWriter.scala 39:21]
  wire  dataQ_io_deq_ready; // @[PacketWriter.scala 39:21]
  wire  dataQ_io_deq_valid; // @[PacketWriter.scala 39:21]
  wire [7:0] dataQ_io_deq_bits_data_0; // @[PacketWriter.scala 39:21]
  wire [7:0] dataQ_io_deq_bits_data_1; // @[PacketWriter.scala 39:21]
  wire [7:0] dataQ_io_deq_bits_data_2; // @[PacketWriter.scala 39:21]
  wire [7:0] dataQ_io_deq_bits_data_3; // @[PacketWriter.scala 39:21]
  reg  interfaceOutReg_slot; // @[PacketWriter.scala 37:28]
  reg  interfaceOutReg_page_pool; // @[PacketWriter.scala 37:28]
  reg [2:0] interfaceOutReg_page_pageNum; // @[PacketWriter.scala 37:28]
  reg [3:0] interfaceOutReg_line; // @[PacketWriter.scala 37:28]
  reg [7:0] interfaceOutReg_data_0; // @[PacketWriter.scala 37:28]
  reg [7:0] interfaceOutReg_data_1; // @[PacketWriter.scala 37:28]
  reg [7:0] interfaceOutReg_data_2; // @[PacketWriter.scala 37:28]
  reg [7:0] interfaceOutReg_data_3; // @[PacketWriter.scala 37:28]
  reg  interfaceOutValid; // @[PacketWriter.scala 38:34]
  reg  state; // @[PacketWriter.scala 41:22]
  reg  currentPage_pool; // @[PacketWriter.scala 43:24]
  reg [2:0] currentPage_pageNum; // @[PacketWriter.scala 43:24]
  reg  schedOutValid; // @[PacketWriter.scala 45:30]
  wire  fsmResourceOk = dataQ_io_enq_ready & io_portDataIn_valid & lineInfoHold_io_enq_ready &
    bufferAllocator_io_freePage_valid; // @[PacketWriter.scala 73:94]
  wire  _T_2 = ~state; // @[Conditional.scala 37:30]
  wire  _T_3 = io_portDataIn_bits_code_code == 2'h0; // @[Types.scala 10:36]
  wire  _T_4 = fsmResourceOk & _T_3; // @[PacketWriter.scala 79:27]
  wire  _GEN_1 = fsmResourceOk & _T_3 | state; // @[PacketWriter.scala 79:63 PacketWriter.scala 80:15 PacketWriter.scala 41:22]
  wire [2:0] _GEN_5 = fsmResourceOk & _T_3 ? bufferAllocator_io_freePage_bits_pageNum : currentPage_pageNum; // @[PacketWriter.scala 79:63 PacketWriter.scala 87:39 PacketWriter.scala 59:33]
  wire  _GEN_6 = fsmResourceOk & _T_3 ? bufferAllocator_io_freePage_bits_pool : currentPage_pool; // @[PacketWriter.scala 79:63 PacketWriter.scala 87:39 PacketWriter.scala 59:33]
  wire  _T_7 = fsmResourceOk & ~schedOutValid; // @[PacketWriter.scala 95:27]
  wire  _T_10 = io_portDataIn_bits_code_code == 2'h2 | io_portDataIn_bits_code_code == 2'h3; // @[Types.scala 11:56]
  wire  _GEN_16 = _T_10 ? 1'h0 : state; // @[PacketWriter.scala 98:48 PacketWriter.scala 102:17 PacketWriter.scala 41:22]
  wire  _GEN_17 = _T_10 | schedOutValid; // @[PacketWriter.scala 98:48 PacketWriter.scala 103:25 PacketWriter.scala 45:30]
  wire  _GEN_34 = state & _T_7; // @[Conditional.scala 39:67 PacketWriter.scala 58:29]
  wire  _T_16 = dataQ_io_deq_valid & lineInfoHold_io_deq_valid & ~io_interface_writeReqIn_bits_slot; // @[PacketWriter.scala 128:91]
  BasicPacketBufferAllocator bufferAllocator ( // @[PacketWriter.scala 34:31]
    .clock(bufferAllocator_clock),
    .reset(bufferAllocator_reset),
    .io_freeListReq_valid(bufferAllocator_io_freeListReq_valid),
    .io_freeListReq_credit(bufferAllocator_io_freeListReq_credit),
    .io_freeListReq_bits_pool(bufferAllocator_io_freeListReq_bits_pool),
    .io_freeListPage_valid(bufferAllocator_io_freeListPage_valid),
    .io_freeListPage_bits_page_pool(bufferAllocator_io_freeListPage_bits_page_pool),
    .io_freeListPage_bits_page_pageNum(bufferAllocator_io_freeListPage_bits_page_pageNum),
    .io_freePage_ready(bufferAllocator_io_freePage_ready),
    .io_freePage_valid(bufferAllocator_io_freePage_valid),
    .io_freePage_bits_pool(bufferAllocator_io_freePage_bits_pool),
    .io_freePage_bits_pageNum(bufferAllocator_io_freePage_bits_pageNum)
  );
  Queue_3 lineInfoHold ( // @[PacketWriter.scala 36:28]
    .clock(lineInfoHold_clock),
    .reset(lineInfoHold_reset),
    .io_enq_ready(lineInfoHold_io_enq_ready),
    .io_enq_valid(lineInfoHold_io_enq_valid),
    .io_enq_bits_page_pool(lineInfoHold_io_enq_bits_page_pool),
    .io_enq_bits_page_pageNum(lineInfoHold_io_enq_bits_page_pageNum),
    .io_deq_ready(lineInfoHold_io_deq_ready),
    .io_deq_valid(lineInfoHold_io_deq_valid),
    .io_deq_bits_page_pool(lineInfoHold_io_deq_bits_page_pool),
    .io_deq_bits_page_pageNum(lineInfoHold_io_deq_bits_page_pageNum),
    .io_deq_bits_line(lineInfoHold_io_deq_bits_line)
  );
  Queue_4 dataQ ( // @[PacketWriter.scala 39:21]
    .clock(dataQ_clock),
    .reset(dataQ_reset),
    .io_enq_ready(dataQ_io_enq_ready),
    .io_enq_valid(dataQ_io_enq_valid),
    .io_enq_bits_data_0(dataQ_io_enq_bits_data_0),
    .io_enq_bits_data_1(dataQ_io_enq_bits_data_1),
    .io_enq_bits_data_2(dataQ_io_enq_bits_data_2),
    .io_enq_bits_data_3(dataQ_io_enq_bits_data_3),
    .io_deq_ready(dataQ_io_deq_ready),
    .io_deq_valid(dataQ_io_deq_valid),
    .io_deq_bits_data_0(dataQ_io_deq_bits_data_0),
    .io_deq_bits_data_1(dataQ_io_deq_bits_data_1),
    .io_deq_bits_data_2(dataQ_io_deq_bits_data_2),
    .io_deq_bits_data_3(dataQ_io_deq_bits_data_3)
  );
  assign io_portDataIn_ready = _T_2 & _T_4; // @[Conditional.scala 40:58 PacketWriter.scala 49:22]
  assign io_interface_freeListReq_valid = bufferAllocator_io_freeListReq_valid; // @[PacketWriter.scala 55:28]
  assign io_interface_freeListReq_bits_pool = bufferAllocator_io_freeListReq_bits_pool; // @[PacketWriter.scala 55:28]
  assign io_interface_writeReqOut_valid = interfaceOutValid; // @[PacketWriter.scala 124:34]
  assign io_interface_writeReqOut_bits_slot = interfaceOutReg_slot; // @[PacketWriter.scala 125:33]
  assign io_interface_writeReqOut_bits_page_pool = interfaceOutReg_page_pool; // @[PacketWriter.scala 125:33]
  assign io_interface_writeReqOut_bits_page_pageNum = interfaceOutReg_page_pageNum; // @[PacketWriter.scala 125:33]
  assign io_interface_writeReqOut_bits_line = interfaceOutReg_line; // @[PacketWriter.scala 125:33]
  assign io_interface_writeReqOut_bits_data_0 = interfaceOutReg_data_0; // @[PacketWriter.scala 125:33]
  assign io_interface_writeReqOut_bits_data_1 = interfaceOutReg_data_1; // @[PacketWriter.scala 125:33]
  assign io_interface_writeReqOut_bits_data_2 = interfaceOutReg_data_2; // @[PacketWriter.scala 125:33]
  assign io_interface_writeReqOut_bits_data_3 = interfaceOutReg_data_3; // @[PacketWriter.scala 125:33]
  assign bufferAllocator_clock = clock;
  assign bufferAllocator_reset = reset;
  assign bufferAllocator_io_freeListReq_credit = io_interface_freeListReq_credit; // @[PacketWriter.scala 55:28]
  assign bufferAllocator_io_freeListPage_valid = io_interface_freeListPage_valid; // @[PacketWriter.scala 56:29]
  assign bufferAllocator_io_freeListPage_bits_page_pool = io_interface_freeListPage_bits_page_pool; // @[PacketWriter.scala 56:29]
  assign bufferAllocator_io_freeListPage_bits_page_pageNum = io_interface_freeListPage_bits_page_pageNum; // @[PacketWriter.scala 56:29]
  assign bufferAllocator_io_freePage_ready = _T_2 & _T_4; // @[Conditional.scala 40:58]
  assign lineInfoHold_clock = clock;
  assign lineInfoHold_reset = reset;
  assign lineInfoHold_io_enq_valid = _T_2 ? _T_4 : _GEN_34; // @[Conditional.scala 40:58]
  assign lineInfoHold_io_enq_bits_page_pool = _T_2 ? _GEN_6 : currentPage_pool; // @[Conditional.scala 40:58 PacketWriter.scala 59:33]
  assign lineInfoHold_io_enq_bits_page_pageNum = _T_2 ? _GEN_5 : currentPage_pageNum; // @[Conditional.scala 40:58 PacketWriter.scala 59:33]
  assign lineInfoHold_io_deq_ready = dataQ_io_deq_valid & lineInfoHold_io_deq_valid & ~io_interface_writeReqIn_bits_slot
    ; // @[PacketWriter.scala 128:91]
  assign dataQ_clock = clock;
  assign dataQ_reset = reset;
  assign dataQ_io_enq_valid = _T_2 & _T_4; // @[Conditional.scala 40:58 PacketWriter.scala 49:22]
  assign dataQ_io_enq_bits_data_0 = io_portDataIn_bits_data_0; // @[PacketWriter.scala 50:21]
  assign dataQ_io_enq_bits_data_1 = io_portDataIn_bits_data_1; // @[PacketWriter.scala 50:21]
  assign dataQ_io_enq_bits_data_2 = io_portDataIn_bits_data_2; // @[PacketWriter.scala 50:21]
  assign dataQ_io_enq_bits_data_3 = io_portDataIn_bits_data_3; // @[PacketWriter.scala 50:21]
  assign dataQ_io_deq_ready = dataQ_io_deq_valid & lineInfoHold_io_deq_valid & ~io_interface_writeReqIn_bits_slot; // @[PacketWriter.scala 128:91]
  always @(posedge clock) begin
    if (dataQ_io_deq_valid & lineInfoHold_io_deq_valid & ~io_interface_writeReqIn_bits_slot) begin // @[PacketWriter.scala 128:138]
      interfaceOutReg_slot <= 1'h0; // @[PacketWriter.scala 132:26]
    end else begin
      interfaceOutReg_slot <= io_interface_writeReqIn_bits_slot; // @[PacketWriter.scala 139:21]
    end
    interfaceOutReg_page_pool <= dataQ_io_deq_valid & lineInfoHold_io_deq_valid & ~io_interface_writeReqIn_bits_slot &
      lineInfoHold_io_deq_bits_page_pool; // @[PacketWriter.scala 128:138 PacketWriter.scala 135:26 PacketWriter.scala 139:21]
    if (dataQ_io_deq_valid & lineInfoHold_io_deq_valid & ~io_interface_writeReqIn_bits_slot) begin // @[PacketWriter.scala 128:138]
      interfaceOutReg_page_pageNum <= lineInfoHold_io_deq_bits_page_pageNum; // @[PacketWriter.scala 135:26]
    end else begin
      interfaceOutReg_page_pageNum <= 3'h0; // @[PacketWriter.scala 139:21]
    end
    if (dataQ_io_deq_valid & lineInfoHold_io_deq_valid & ~io_interface_writeReqIn_bits_slot) begin // @[PacketWriter.scala 128:138]
      interfaceOutReg_line <= lineInfoHold_io_deq_bits_line; // @[PacketWriter.scala 134:26]
    end else begin
      interfaceOutReg_line <= 4'h0; // @[PacketWriter.scala 139:21]
    end
    if (dataQ_io_deq_valid & lineInfoHold_io_deq_valid & ~io_interface_writeReqIn_bits_slot) begin // @[PacketWriter.scala 128:138]
      interfaceOutReg_data_0 <= dataQ_io_deq_bits_data_0; // @[PacketWriter.scala 133:26]
    end else begin
      interfaceOutReg_data_0 <= 8'h0; // @[PacketWriter.scala 139:21]
    end
    if (dataQ_io_deq_valid & lineInfoHold_io_deq_valid & ~io_interface_writeReqIn_bits_slot) begin // @[PacketWriter.scala 128:138]
      interfaceOutReg_data_1 <= dataQ_io_deq_bits_data_1; // @[PacketWriter.scala 133:26]
    end else begin
      interfaceOutReg_data_1 <= 8'h0; // @[PacketWriter.scala 139:21]
    end
    if (dataQ_io_deq_valid & lineInfoHold_io_deq_valid & ~io_interface_writeReqIn_bits_slot) begin // @[PacketWriter.scala 128:138]
      interfaceOutReg_data_2 <= dataQ_io_deq_bits_data_2; // @[PacketWriter.scala 133:26]
    end else begin
      interfaceOutReg_data_2 <= 8'h0; // @[PacketWriter.scala 139:21]
    end
    if (dataQ_io_deq_valid & lineInfoHold_io_deq_valid & ~io_interface_writeReqIn_bits_slot) begin // @[PacketWriter.scala 128:138]
      interfaceOutReg_data_3 <= dataQ_io_deq_bits_data_3; // @[PacketWriter.scala 133:26]
    end else begin
      interfaceOutReg_data_3 <= 8'h0; // @[PacketWriter.scala 139:21]
    end
    if (reset) begin // @[PacketWriter.scala 38:34]
      interfaceOutValid <= 1'h0; // @[PacketWriter.scala 38:34]
    end else begin
      interfaceOutValid <= _T_16;
    end
    if (reset) begin // @[PacketWriter.scala 41:22]
      state <= 1'h0; // @[PacketWriter.scala 41:22]
    end else if (_T_2) begin // @[Conditional.scala 40:58]
      state <= _GEN_1;
    end else if (state) begin // @[Conditional.scala 39:67]
      if (fsmResourceOk & ~schedOutValid) begin // @[PacketWriter.scala 95:46]
        state <= _GEN_16;
      end
    end
    if (_T_2) begin // @[Conditional.scala 40:58]
      if (fsmResourceOk & _T_3) begin // @[PacketWriter.scala 79:63]
        currentPage_pool <= bufferAllocator_io_freePage_bits_pool; // @[PacketWriter.scala 87:39]
      end
    end
    if (_T_2) begin // @[Conditional.scala 40:58]
      if (fsmResourceOk & _T_3) begin // @[PacketWriter.scala 79:63]
        currentPage_pageNum <= bufferAllocator_io_freePage_bits_pageNum; // @[PacketWriter.scala 87:39]
      end
    end
    if (reset) begin // @[PacketWriter.scala 45:30]
      schedOutValid <= 1'h0; // @[PacketWriter.scala 45:30]
    end else if (!(_T_2)) begin // @[Conditional.scala 40:58]
      if (state) begin // @[Conditional.scala 39:67]
        if (fsmResourceOk & ~schedOutValid) begin // @[PacketWriter.scala 95:46]
          schedOutValid <= _GEN_17;
        end
      end
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  interfaceOutReg_slot = _RAND_0[0:0];
  _RAND_1 = {1{`RANDOM}};
  interfaceOutReg_page_pool = _RAND_1[0:0];
  _RAND_2 = {1{`RANDOM}};
  interfaceOutReg_page_pageNum = _RAND_2[2:0];
  _RAND_3 = {1{`RANDOM}};
  interfaceOutReg_line = _RAND_3[3:0];
  _RAND_4 = {1{`RANDOM}};
  interfaceOutReg_data_0 = _RAND_4[7:0];
  _RAND_5 = {1{`RANDOM}};
  interfaceOutReg_data_1 = _RAND_5[7:0];
  _RAND_6 = {1{`RANDOM}};
  interfaceOutReg_data_2 = _RAND_6[7:0];
  _RAND_7 = {1{`RANDOM}};
  interfaceOutReg_data_3 = _RAND_7[7:0];
  _RAND_8 = {1{`RANDOM}};
  interfaceOutValid = _RAND_8[0:0];
  _RAND_9 = {1{`RANDOM}};
  state = _RAND_9[0:0];
  _RAND_10 = {1{`RANDOM}};
  currentPage_pool = _RAND_10[0:0];
  _RAND_11 = {1{`RANDOM}};
  currentPage_pageNum = _RAND_11[2:0];
  _RAND_12 = {1{`RANDOM}};
  schedOutValid = _RAND_12[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module PacketWriterTestbench(
  input         clock,
  input         reset,
  output        io_sendPacket_ready,
  input         io_sendPacket_valid,
  input  [15:0] io_sendPacket_bits_length,
  input  [15:0] io_sendPacket_bits_pid,
  input         io_sendPacket_bits_packetGood,
  output        io_writeReqOut_valid,
  output        io_writeReqOut_bits_slot,
  output        io_writeReqOut_bits_page_pool,
  output [2:0]  io_writeReqOut_bits_page_pageNum,
  output [3:0]  io_writeReqOut_bits_line,
  output [7:0]  io_writeReqOut_bits_data_0,
  output [7:0]  io_writeReqOut_bits_data_1,
  output [7:0]  io_writeReqOut_bits_data_2,
  output [7:0]  io_writeReqOut_bits_data_3,
  output        io_error
);
  wire  sender_clock; // @[PacketWriterTester.scala 74:22]
  wire  sender_reset; // @[PacketWriterTester.scala 74:22]
  wire  sender_io_packetData_ready; // @[PacketWriterTester.scala 74:22]
  wire  sender_io_packetData_valid; // @[PacketWriterTester.scala 74:22]
  wire [7:0] sender_io_packetData_bits_data_0; // @[PacketWriterTester.scala 74:22]
  wire [7:0] sender_io_packetData_bits_data_1; // @[PacketWriterTester.scala 74:22]
  wire [7:0] sender_io_packetData_bits_data_2; // @[PacketWriterTester.scala 74:22]
  wire [7:0] sender_io_packetData_bits_data_3; // @[PacketWriterTester.scala 74:22]
  wire [1:0] sender_io_packetData_bits_code_code; // @[PacketWriterTester.scala 74:22]
  wire  sender_io_sendPacket_ready; // @[PacketWriterTester.scala 74:22]
  wire  sender_io_sendPacket_valid; // @[PacketWriterTester.scala 74:22]
  wire [15:0] sender_io_sendPacket_bits_length; // @[PacketWriterTester.scala 74:22]
  wire  sender_io_sendPacket_bits_packetGood; // @[PacketWriterTester.scala 74:22]
  wire  ifStub_clock; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_reset; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_io_interface_freeListReq_valid; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_io_interface_freeListReq_credit; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_io_interface_freeListReq_bits_pool; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_io_interface_freeListPage_valid; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_io_interface_freeListPage_bits_page_pool; // @[PacketWriterTester.scala 75:22]
  wire [2:0] ifStub_io_interface_freeListPage_bits_page_pageNum; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_io_interface_writeReqIn_bits_slot; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_io_interface_writeReqOut_valid; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_io_interface_writeReqOut_bits_slot; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_io_interface_writeReqOut_bits_page_pool; // @[PacketWriterTester.scala 75:22]
  wire [2:0] ifStub_io_interface_writeReqOut_bits_page_pageNum; // @[PacketWriterTester.scala 75:22]
  wire [3:0] ifStub_io_interface_writeReqOut_bits_line; // @[PacketWriterTester.scala 75:22]
  wire [7:0] ifStub_io_interface_writeReqOut_bits_data_0; // @[PacketWriterTester.scala 75:22]
  wire [7:0] ifStub_io_interface_writeReqOut_bits_data_1; // @[PacketWriterTester.scala 75:22]
  wire [7:0] ifStub_io_interface_writeReqOut_bits_data_2; // @[PacketWriterTester.scala 75:22]
  wire [7:0] ifStub_io_interface_writeReqOut_bits_data_3; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_io_writeReqOut_valid; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_io_writeReqOut_bits_slot; // @[PacketWriterTester.scala 75:22]
  wire  ifStub_io_writeReqOut_bits_page_pool; // @[PacketWriterTester.scala 75:22]
  wire [2:0] ifStub_io_writeReqOut_bits_page_pageNum; // @[PacketWriterTester.scala 75:22]
  wire [3:0] ifStub_io_writeReqOut_bits_line; // @[PacketWriterTester.scala 75:22]
  wire [7:0] ifStub_io_writeReqOut_bits_data_0; // @[PacketWriterTester.scala 75:22]
  wire [7:0] ifStub_io_writeReqOut_bits_data_1; // @[PacketWriterTester.scala 75:22]
  wire [7:0] ifStub_io_writeReqOut_bits_data_2; // @[PacketWriterTester.scala 75:22]
  wire [7:0] ifStub_io_writeReqOut_bits_data_3; // @[PacketWriterTester.scala 75:22]
  wire  dut_clock; // @[PacketWriterTester.scala 76:19]
  wire  dut_reset; // @[PacketWriterTester.scala 76:19]
  wire  dut_io_portDataIn_ready; // @[PacketWriterTester.scala 76:19]
  wire  dut_io_portDataIn_valid; // @[PacketWriterTester.scala 76:19]
  wire [7:0] dut_io_portDataIn_bits_data_0; // @[PacketWriterTester.scala 76:19]
  wire [7:0] dut_io_portDataIn_bits_data_1; // @[PacketWriterTester.scala 76:19]
  wire [7:0] dut_io_portDataIn_bits_data_2; // @[PacketWriterTester.scala 76:19]
  wire [7:0] dut_io_portDataIn_bits_data_3; // @[PacketWriterTester.scala 76:19]
  wire [1:0] dut_io_portDataIn_bits_code_code; // @[PacketWriterTester.scala 76:19]
  wire  dut_io_interface_freeListReq_valid; // @[PacketWriterTester.scala 76:19]
  wire  dut_io_interface_freeListReq_credit; // @[PacketWriterTester.scala 76:19]
  wire  dut_io_interface_freeListReq_bits_pool; // @[PacketWriterTester.scala 76:19]
  wire  dut_io_interface_freeListPage_valid; // @[PacketWriterTester.scala 76:19]
  wire  dut_io_interface_freeListPage_bits_page_pool; // @[PacketWriterTester.scala 76:19]
  wire [2:0] dut_io_interface_freeListPage_bits_page_pageNum; // @[PacketWriterTester.scala 76:19]
  wire  dut_io_interface_writeReqIn_bits_slot; // @[PacketWriterTester.scala 76:19]
  wire  dut_io_interface_writeReqOut_valid; // @[PacketWriterTester.scala 76:19]
  wire  dut_io_interface_writeReqOut_bits_slot; // @[PacketWriterTester.scala 76:19]
  wire  dut_io_interface_writeReqOut_bits_page_pool; // @[PacketWriterTester.scala 76:19]
  wire [2:0] dut_io_interface_writeReqOut_bits_page_pageNum; // @[PacketWriterTester.scala 76:19]
  wire [3:0] dut_io_interface_writeReqOut_bits_line; // @[PacketWriterTester.scala 76:19]
  wire [7:0] dut_io_interface_writeReqOut_bits_data_0; // @[PacketWriterTester.scala 76:19]
  wire [7:0] dut_io_interface_writeReqOut_bits_data_1; // @[PacketWriterTester.scala 76:19]
  wire [7:0] dut_io_interface_writeReqOut_bits_data_2; // @[PacketWriterTester.scala 76:19]
  wire [7:0] dut_io_interface_writeReqOut_bits_data_3; // @[PacketWriterTester.scala 76:19]
  PacketSender sender ( // @[PacketWriterTester.scala 74:22]
    .clock(sender_clock),
    .reset(sender_reset),
    .io_packetData_ready(sender_io_packetData_ready),
    .io_packetData_valid(sender_io_packetData_valid),
    .io_packetData_bits_data_0(sender_io_packetData_bits_data_0),
    .io_packetData_bits_data_1(sender_io_packetData_bits_data_1),
    .io_packetData_bits_data_2(sender_io_packetData_bits_data_2),
    .io_packetData_bits_data_3(sender_io_packetData_bits_data_3),
    .io_packetData_bits_code_code(sender_io_packetData_bits_code_code),
    .io_sendPacket_ready(sender_io_sendPacket_ready),
    .io_sendPacket_valid(sender_io_sendPacket_valid),
    .io_sendPacket_bits_length(sender_io_sendPacket_bits_length),
    .io_sendPacket_bits_packetGood(sender_io_sendPacket_bits_packetGood)
  );
  BufferInterfaceStub ifStub ( // @[PacketWriterTester.scala 75:22]
    .clock(ifStub_clock),
    .reset(ifStub_reset),
    .io_interface_freeListReq_valid(ifStub_io_interface_freeListReq_valid),
    .io_interface_freeListReq_credit(ifStub_io_interface_freeListReq_credit),
    .io_interface_freeListReq_bits_pool(ifStub_io_interface_freeListReq_bits_pool),
    .io_interface_freeListPage_valid(ifStub_io_interface_freeListPage_valid),
    .io_interface_freeListPage_bits_page_pool(ifStub_io_interface_freeListPage_bits_page_pool),
    .io_interface_freeListPage_bits_page_pageNum(ifStub_io_interface_freeListPage_bits_page_pageNum),
    .io_interface_writeReqIn_bits_slot(ifStub_io_interface_writeReqIn_bits_slot),
    .io_interface_writeReqOut_valid(ifStub_io_interface_writeReqOut_valid),
    .io_interface_writeReqOut_bits_slot(ifStub_io_interface_writeReqOut_bits_slot),
    .io_interface_writeReqOut_bits_page_pool(ifStub_io_interface_writeReqOut_bits_page_pool),
    .io_interface_writeReqOut_bits_page_pageNum(ifStub_io_interface_writeReqOut_bits_page_pageNum),
    .io_interface_writeReqOut_bits_line(ifStub_io_interface_writeReqOut_bits_line),
    .io_interface_writeReqOut_bits_data_0(ifStub_io_interface_writeReqOut_bits_data_0),
    .io_interface_writeReqOut_bits_data_1(ifStub_io_interface_writeReqOut_bits_data_1),
    .io_interface_writeReqOut_bits_data_2(ifStub_io_interface_writeReqOut_bits_data_2),
    .io_interface_writeReqOut_bits_data_3(ifStub_io_interface_writeReqOut_bits_data_3),
    .io_writeReqOut_valid(ifStub_io_writeReqOut_valid),
    .io_writeReqOut_bits_slot(ifStub_io_writeReqOut_bits_slot),
    .io_writeReqOut_bits_page_pool(ifStub_io_writeReqOut_bits_page_pool),
    .io_writeReqOut_bits_page_pageNum(ifStub_io_writeReqOut_bits_page_pageNum),
    .io_writeReqOut_bits_line(ifStub_io_writeReqOut_bits_line),
    .io_writeReqOut_bits_data_0(ifStub_io_writeReqOut_bits_data_0),
    .io_writeReqOut_bits_data_1(ifStub_io_writeReqOut_bits_data_1),
    .io_writeReqOut_bits_data_2(ifStub_io_writeReqOut_bits_data_2),
    .io_writeReqOut_bits_data_3(ifStub_io_writeReqOut_bits_data_3)
  );
  PacketWriter dut ( // @[PacketWriterTester.scala 76:19]
    .clock(dut_clock),
    .reset(dut_reset),
    .io_portDataIn_ready(dut_io_portDataIn_ready),
    .io_portDataIn_valid(dut_io_portDataIn_valid),
    .io_portDataIn_bits_data_0(dut_io_portDataIn_bits_data_0),
    .io_portDataIn_bits_data_1(dut_io_portDataIn_bits_data_1),
    .io_portDataIn_bits_data_2(dut_io_portDataIn_bits_data_2),
    .io_portDataIn_bits_data_3(dut_io_portDataIn_bits_data_3),
    .io_portDataIn_bits_code_code(dut_io_portDataIn_bits_code_code),
    .io_interface_freeListReq_valid(dut_io_interface_freeListReq_valid),
    .io_interface_freeListReq_credit(dut_io_interface_freeListReq_credit),
    .io_interface_freeListReq_bits_pool(dut_io_interface_freeListReq_bits_pool),
    .io_interface_freeListPage_valid(dut_io_interface_freeListPage_valid),
    .io_interface_freeListPage_bits_page_pool(dut_io_interface_freeListPage_bits_page_pool),
    .io_interface_freeListPage_bits_page_pageNum(dut_io_interface_freeListPage_bits_page_pageNum),
    .io_interface_writeReqIn_bits_slot(dut_io_interface_writeReqIn_bits_slot),
    .io_interface_writeReqOut_valid(dut_io_interface_writeReqOut_valid),
    .io_interface_writeReqOut_bits_slot(dut_io_interface_writeReqOut_bits_slot),
    .io_interface_writeReqOut_bits_page_pool(dut_io_interface_writeReqOut_bits_page_pool),
    .io_interface_writeReqOut_bits_page_pageNum(dut_io_interface_writeReqOut_bits_page_pageNum),
    .io_interface_writeReqOut_bits_line(dut_io_interface_writeReqOut_bits_line),
    .io_interface_writeReqOut_bits_data_0(dut_io_interface_writeReqOut_bits_data_0),
    .io_interface_writeReqOut_bits_data_1(dut_io_interface_writeReqOut_bits_data_1),
    .io_interface_writeReqOut_bits_data_2(dut_io_interface_writeReqOut_bits_data_2),
    .io_interface_writeReqOut_bits_data_3(dut_io_interface_writeReqOut_bits_data_3)
  );
  assign io_sendPacket_ready = sender_io_sendPacket_ready; // @[PacketWriterTester.scala 78:17]
  assign io_writeReqOut_valid = ifStub_io_writeReqOut_valid; // @[PacketWriterTester.scala 79:18]
  assign io_writeReqOut_bits_slot = ifStub_io_writeReqOut_bits_slot; // @[PacketWriterTester.scala 79:18]
  assign io_writeReqOut_bits_page_pool = ifStub_io_writeReqOut_bits_page_pool; // @[PacketWriterTester.scala 79:18]
  assign io_writeReqOut_bits_page_pageNum = ifStub_io_writeReqOut_bits_page_pageNum; // @[PacketWriterTester.scala 79:18]
  assign io_writeReqOut_bits_line = ifStub_io_writeReqOut_bits_line; // @[PacketWriterTester.scala 79:18]
  assign io_writeReqOut_bits_data_0 = ifStub_io_writeReqOut_bits_data_0; // @[PacketWriterTester.scala 79:18]
  assign io_writeReqOut_bits_data_1 = ifStub_io_writeReqOut_bits_data_1; // @[PacketWriterTester.scala 79:18]
  assign io_writeReqOut_bits_data_2 = ifStub_io_writeReqOut_bits_data_2; // @[PacketWriterTester.scala 79:18]
  assign io_writeReqOut_bits_data_3 = ifStub_io_writeReqOut_bits_data_3; // @[PacketWriterTester.scala 79:18]
  assign io_error = 1'h0; // @[PacketWriterTester.scala 90:12]
  assign sender_clock = clock;
  assign sender_reset = reset;
  assign sender_io_packetData_ready = dut_io_portDataIn_ready; // @[PacketWriterTester.scala 81:24]
  assign sender_io_sendPacket_valid = io_sendPacket_valid; // @[PacketWriterTester.scala 78:17]
  assign sender_io_sendPacket_bits_length = io_sendPacket_bits_length; // @[PacketWriterTester.scala 78:17]
  assign sender_io_sendPacket_bits_packetGood = io_sendPacket_bits_packetGood; // @[PacketWriterTester.scala 78:17]
  assign ifStub_clock = clock;
  assign ifStub_reset = reset;
  assign ifStub_io_interface_freeListReq_valid = dut_io_interface_freeListReq_valid; // @[PacketWriterTester.scala 82:20]
  assign ifStub_io_interface_freeListReq_bits_pool = dut_io_interface_freeListReq_bits_pool; // @[PacketWriterTester.scala 82:20]
  assign ifStub_io_interface_writeReqOut_valid = dut_io_interface_writeReqOut_valid; // @[PacketWriterTester.scala 82:20]
  assign ifStub_io_interface_writeReqOut_bits_slot = dut_io_interface_writeReqOut_bits_slot; // @[PacketWriterTester.scala 82:20]
  assign ifStub_io_interface_writeReqOut_bits_page_pool = dut_io_interface_writeReqOut_bits_page_pool; // @[PacketWriterTester.scala 82:20]
  assign ifStub_io_interface_writeReqOut_bits_page_pageNum = dut_io_interface_writeReqOut_bits_page_pageNum; // @[PacketWriterTester.scala 82:20]
  assign ifStub_io_interface_writeReqOut_bits_line = dut_io_interface_writeReqOut_bits_line; // @[PacketWriterTester.scala 82:20]
  assign ifStub_io_interface_writeReqOut_bits_data_0 = dut_io_interface_writeReqOut_bits_data_0; // @[PacketWriterTester.scala 82:20]
  assign ifStub_io_interface_writeReqOut_bits_data_1 = dut_io_interface_writeReqOut_bits_data_1; // @[PacketWriterTester.scala 82:20]
  assign ifStub_io_interface_writeReqOut_bits_data_2 = dut_io_interface_writeReqOut_bits_data_2; // @[PacketWriterTester.scala 82:20]
  assign ifStub_io_interface_writeReqOut_bits_data_3 = dut_io_interface_writeReqOut_bits_data_3; // @[PacketWriterTester.scala 82:20]
  assign dut_clock = clock;
  assign dut_reset = reset;
  assign dut_io_portDataIn_valid = sender_io_packetData_valid; // @[PacketWriterTester.scala 81:24]
  assign dut_io_portDataIn_bits_data_0 = sender_io_packetData_bits_data_0; // @[PacketWriterTester.scala 81:24]
  assign dut_io_portDataIn_bits_data_1 = sender_io_packetData_bits_data_1; // @[PacketWriterTester.scala 81:24]
  assign dut_io_portDataIn_bits_data_2 = sender_io_packetData_bits_data_2; // @[PacketWriterTester.scala 81:24]
  assign dut_io_portDataIn_bits_data_3 = sender_io_packetData_bits_data_3; // @[PacketWriterTester.scala 81:24]
  assign dut_io_portDataIn_bits_code_code = sender_io_packetData_bits_code_code; // @[PacketWriterTester.scala 81:24]
  assign dut_io_interface_freeListReq_credit = ifStub_io_interface_freeListReq_credit; // @[PacketWriterTester.scala 82:20]
  assign dut_io_interface_freeListPage_valid = ifStub_io_interface_freeListPage_valid; // @[PacketWriterTester.scala 82:20]
  assign dut_io_interface_freeListPage_bits_page_pool = ifStub_io_interface_freeListPage_bits_page_pool; // @[PacketWriterTester.scala 82:20]
  assign dut_io_interface_freeListPage_bits_page_pageNum = ifStub_io_interface_freeListPage_bits_page_pageNum; // @[PacketWriterTester.scala 82:20]
  assign dut_io_interface_writeReqIn_bits_slot = ifStub_io_interface_writeReqIn_bits_slot; // @[PacketWriterTester.scala 82:20]
endmodule
