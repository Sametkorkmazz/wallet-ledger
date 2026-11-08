import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { signInSchema, type SignInValues } from "@/schemas/auth";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
export default function SignInForm() {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SignInValues>({
    resolver: zodResolver(signInSchema),
  });
  const navigate = useNavigate();
  function onSubmit() {
    // TODO: call api.signIn
    navigate("/dashboard");
  }
  return (
    <form
      className="flex flex-col gap-5"
      noValidate
      onSubmit={handleSubmit(onSubmit)}
    >
      <div className="flex flex-col gap-1.5">
        <Label htmlFor="si-email">Email</Label>
        <Input
          aria-invalid={!!errors.email}
          placeholder="you@example.com"
          autoComplete="email"
          id="si-email"
          {...register("email")}
        />
        {errors.email && (
          <p className="text-sm text-danger">{errors.email?.message}</p>
        )}
      </div>
      <div className="flex flex-col gap-1.5">
        <Label htmlFor="si-password">Password</Label>
        <Input
          aria-invalid={!!errors.password}
          autoComplete="current-password"
          id="si-password"
          type="password"
          {...register("password")}
        />
        {errors.password && (
          <p className="text-sm text-danger">{errors.password?.message}</p>
        )}
      </div>
      <Button type="submit" size={"lg"}>
        Sign in
      </Button>
    </form>
  );
}
