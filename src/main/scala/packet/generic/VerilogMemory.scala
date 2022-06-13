package packet.generic

import chisel3.util._
import chisel3._

class VerilogMemory1RW[D <: Data](dtype: D, words: Int, rlat: Int=1) extends Memory1RW(dtype, words, rlat) {
  val minst = Module(new behave1p_mem(words, dtype.getWidth, log2Ceil(words)))

  minst.io.clk := clock.asBool
  minst.io.wr_en := io.writeEnable
  minst.io.rd_en := io.readEnable
  minst.io.addr := io.addr
  minst.io.d_in := io.writeData.asUInt
  io.readData := minst.io.d_out.asTypeOf(dtype.cloneType)
}

class VerilogMemgen1RW extends Memgen1RW {
  override def apply[D <: Data](dtype: D, depth: Int, latency: Int=1) : Memory1RW[D] = {
    new VerilogMemory1RW(dtype, depth, latency)
  }
}

class behave1p_mem(depth : Int, width : Int, addr_sz : Int) extends BlackBox(Map("depth" -> depth, "width" -> width, "addr_sz" -> addr_sz)) with HasBlackBoxInline {
  val io = IO(new Bundle {
    val wr_en = Input(Bool())
    val rd_en = Input(Bool())
    val clk = Input(Bool())
    val d_in = Input(UInt(width.W))
    val addr = Input(UInt(addr_sz.W))
    val d_out = Output(UInt(width.W))
  })
  setInline("behave1p_mem.v",
    """
      |module behave1p_mem
      |  #(parameter depth=256,
      |    parameter width=8,
      |    parameter addr_sz=$clog2(depth))
      |  (/*AUTOARG*/
      |  // Outputs
      |  d_out,
      |  // Inputs
      |  wr_en, rd_en, clk, d_in, addr
      |  );
      |  input        wr_en, rd_en, clk;
      |  input [width-1:0] d_in;
      |  input [addr_sz-1:0]     addr;
      |
      |  output [width-1:0]     d_out;
      |
      |  reg [addr_sz-1:0] r_addr;
      |
      |  reg [width-1:0]            array[0:depth-1];
      |
      |  always @(posedge clk)
      |    begin
      |      if (wr_en)
      |        begin
      |          array[addr] <= #1 d_in;
      |        end
      |      else if (rd_en)
      |        begin
      |          r_addr <= #1 addr;
      |        end
      |    end // always @ (posedge clk)
      |
      |  assign d_out = array[r_addr];
      |
      |endmodule
      |""".stripMargin)
}

class behave2p_mem(depth : Int, width : Int, addr_sz : Int) extends BlackBox(Map("depth" -> depth, "width" -> width, "addr_sz" -> addr_sz)) with HasBlackBoxInline {
  val io = IO(new Bundle {
    val wr_en = Input(Bool())
    val wr_clk = Input(Bool())
    val rd_clk = Input(Bool())
    val rd_en = Input(Bool())
    val d_in = Input(UInt(width.W))
    val rd_addr = Input(UInt(addr_sz.W))
    val d_out = Output(UInt(width.W))
  })
  setInline("behave1p_mem.v",
    """
      |module behave2p_mem
      |  #(parameter width=8,
      |    parameter depth=256,
      |    parameter addr_sz=$clog2(depth),
      |    parameter reg_rd_addr=1)
      |  (/*AUTOARG*/
      |  // Outputs
      |  d_out,
      |  // Inputs
      |  wr_en, rd_en, wr_clk, rd_clk, d_in, rd_addr, wr_addr
      |  );
      |  input        wr_en, rd_en, wr_clk;
      |  input        rd_clk;
      |  input [width-1:0] d_in;
      |  input [addr_sz-1:0] rd_addr, wr_addr;
      |
      |  output [width-1:0]  d_out;
      |
      |  logic [addr_sz-1:0] r_addr;
      |
      |  reg [width-1:0]   array[0:depth-1];
      |
      |  always @(posedge wr_clk)
      |    begin
      |      if (wr_en)
      |        begin
      |          array[wr_addr] <= `SDLIB_DELAY d_in;   // ri lint_check_waive VAR_INDEX_RANGE
      |        end
      |    end
      |
      |  generate if (reg_rd_addr == 1)
      |    begin : gen_reg_rd
      |      always @(posedge rd_clk)
      |        begin
      |          if (rd_en)
      |            begin
      |              r_addr <= `SDLIB_DELAY rd_addr;
      |            end
      |        end // always @ (posedge clk)
      |    end
      |  else
      |    begin : gen_noreg_rd
      |      assign r_addr = rd_addr;
      |    end
      |  endgenerate
      |
      |  assign d_out = array[r_addr]; // ri lint_check_waive VAR_INDEX_RANGE
      |endmodule
      |
      |""".stripMargin)
}